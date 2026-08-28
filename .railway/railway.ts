import { defineRailway, project, service, postgres, redis, mongo, image, github, volume } from "railway/iac";

export default defineRailway(() => {
  const REPO = "mayconaraujosantos/takeoutfood_ecommerce";
  const BRANCH = "main";
  const repoSource = github(REPO, { branch: BRANCH });

  // Generated once for this deploy: both auth-service (signs tokens) and
  // api-gateway (validates them) must share the exact same value.
  const JWT_SECRET =
    "93b853a6a4b1f7cc09880a669e747044e827d1856afff60283e93a82180536839d5eea8079b33162ebc2029423a4ff5c";

  // ---- Managed databases ----
  const db = postgres("postgres");
  const cache = redis("redis");
  const mongoDb = mongo("mongo");

  // Railway's own db.env.DATABASE_URL is "postgresql://..." (no jdbc: prefix),
  // which every service's spring.datasource.url rejects. Build our own.
  const postgresJdbcUrl =
    "jdbc:postgresql://${{postgres.PGHOST}}:${{postgres.PGPORT}}/${{postgres.PGDATABASE}}";

  // ---- Kafka (no native Railway resource, deployed as plain Docker images) ----
  const zookeeperVolume = volume("zookeeper-data");
  const zookeeper = service("zookeeper", {
    source: image("confluentinc/cp-zookeeper:7.4.0"),
    env: {
      ZOOKEEPER_CLIENT_PORT: "2181",
      ZOOKEEPER_TICK_TIME: "2000",
    },
    volumeMounts: {
      "zookeeper-data": { volume: zookeeperVolume, mountPath: "/var/lib/zookeeper/data" },
    },
    deploy: { restartPolicyType: "ALWAYS" },
  });

  const kafkaVolume = volume("kafka-data");
  const kafka = service("kafka", {
    source: image("confluentinc/cp-kafka:7.4.0"),
    env: {
      KAFKA_BROKER_ID: "1",
      KAFKA_ZOOKEEPER_CONNECT: "${{zookeeper.RAILWAY_PRIVATE_DOMAIN}}:2181",
      // Fixes the bug found locally: must advertise Kafka's own private
      // network address, not localhost, so other services can reach it.
      KAFKA_ADVERTISED_LISTENERS: "PLAINTEXT://${{kafka.RAILWAY_PRIVATE_DOMAIN}}:9092",
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "1",
    },
    volumeMounts: {
      "kafka-data": { volume: kafkaVolume, mountPath: "/var/lib/kafka/data" },
    },
    deploy: { restartPolicyType: "ALWAYS" },
  });

  const kafkaBootstrap = "${{kafka.RAILWAY_PRIVATE_DOMAIN}}:9092";

  // ---- Core platform services ----
  const configServer = service("config-server", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "config-server/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "native",
      // The app doesn't read $PORT (fixed port per service), so Railway
      // needs this explicitly to know where to target its healthcheck.
      // See: docs.railway.com/deployments/healthchecks#configure-the-healthcheck-port
      PORT: "8888",
    },
  });

  const configServerUrl = "http://${{config-server.RAILWAY_PRIVATE_DOMAIN}}:8888";

  const serviceDiscovery = service("service-discovery", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "service-discovery/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      PORT: "8761",
    },
  });

  const eurekaUrl = "http://${{service-discovery.RAILWAY_PRIVATE_DOMAIN}}:8761";

  const apiGateway = service("api-gateway", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "api-gateway/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "prod",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      REDIS_HOST: cache.env.REDISHOST,
      REDIS_PORT: cache.env.REDISPORT,
      REDIS_USER: cache.env.REDISUSER,
      REDIS_PASSWORD: cache.env.REDISPASSWORD,
      SPRING_DATA_REDIS_HOST: cache.env.REDISHOST,
      SPRING_DATA_REDIS_PORT: cache.env.REDISPORT,
      JWT_SECRET,
      PORT: "8080",
    },
  });

  // ---- Business services ----
  const authService = service("auth-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "auth-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "dev",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      DATABASE_URL: postgresJdbcUrl,
      DB_HOST: db.env.PGHOST,
      DB_PORT: db.env.PGPORT,
      DB_NAME: db.env.PGDATABASE,
      DB_USER: db.env.PGUSER,
      DB_PASSWORD: db.env.PGPASSWORD,
      REDIS_HOST: cache.env.REDISHOST,
      REDIS_PORT: cache.env.REDISPORT,
      REDIS_USER: cache.env.REDISUSER,
      REDIS_PASSWORD: cache.env.REDISPASSWORD,
      SPRING_DATA_REDIS_HOST: cache.env.REDISHOST,
      SPRING_DATA_REDIS_PORT: cache.env.REDISPORT,
      JWT_SECRET,
      PORT: "8081",
    },
  });

  const userService = service("user-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "user-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      DATABASE_URL: postgresJdbcUrl,
      DB_USERNAME: db.env.PGUSER,
      DB_PASSWORD: db.env.PGPASSWORD,
      KAFKA_BOOTSTRAP_SERVERS: kafkaBootstrap,
      PORT: "8082",
    },
  });

  const restaurantService = service("restaurant-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "restaurant-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      DATABASE_URL: postgresJdbcUrl,
      DB_USERNAME: db.env.PGUSER,
      DB_PASSWORD: db.env.PGPASSWORD,
      KAFKA_BOOTSTRAP_SERVERS: kafkaBootstrap,
      PORT: "8083",
    },
  });

  const menuService = service("menu-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "menu-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      DATABASE_URL: postgresJdbcUrl,
      DB_USERNAME: db.env.PGUSER,
      DB_PASSWORD: db.env.PGPASSWORD,
      KAFKA_BOOTSTRAP_SERVERS: kafkaBootstrap,
      PORT: "8084",
    },
  });

  const orderService = service("order-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "order-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      DATABASE_URL: postgresJdbcUrl,
      DB_USERNAME: db.env.PGUSER,
      DB_PASSWORD: db.env.PGPASSWORD,
      KAFKA_BOOTSTRAP_SERVERS: kafkaBootstrap,
      PORT: "8085",
    },
  });

  const paymentService = service("payment-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "payment-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      DATABASE_URL: postgresJdbcUrl,
      DB_USERNAME: db.env.PGUSER,
      DB_PASSWORD: db.env.PGPASSWORD,
      KAFKA_BOOTSTRAP_SERVERS: kafkaBootstrap,
      PORT: "8086",
    },
  });

  const deliveryService = service("delivery-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "delivery-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      DATABASE_URL: postgresJdbcUrl,
      DB_USERNAME: db.env.PGUSER,
      DB_PASSWORD: db.env.PGPASSWORD,
      KAFKA_BOOTSTRAP_SERVERS: kafkaBootstrap,
      PORT: "8088",
    },
  });

  const notificationService = service("notification-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "notification-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      MONGODB_URI:
        "mongodb://${{mongo.MONGOUSER}}:${{mongo.MONGOPASSWORD}}@${{mongo.RAILWAY_PRIVATE_DOMAIN}}:27017/ifood_notifications",
      KAFKA_BOOTSTRAP_SERVERS: kafkaBootstrap,
      PORT: "8087",
    },
  });

  const reviewService = service("review-service", {
    source: repoSource,
    build: { builder: "DOCKERFILE", dockerfilePath: "review-service/Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 120,
    deploy: { restartPolicyType: "ON_FAILURE", restartPolicyMaxRetries: 3 },
    env: {
      SPRING_PROFILES_ACTIVE: "docker",
      CONFIG_SERVER_URL: configServerUrl,
      EUREKA_SERVER_URL: eurekaUrl,
      MONGODB_URI:
        "mongodb://${{mongo.MONGOUSER}}:${{mongo.MONGOPASSWORD}}@${{mongo.RAILWAY_PRIVATE_DOMAIN}}:27017/ifood_reviews",
      KAFKA_BOOTSTRAP_SERVERS: kafkaBootstrap,
      PORT: "8089",
    },
  });

  return project("takeoutfood-ecommerce", {
    resources: [
      db,
      cache,
      mongoDb,
      zookeeper,
      kafka,
      configServer,
      serviceDiscovery,
      apiGateway,
      authService,
      userService,
      restaurantService,
      menuService,
      orderService,
      paymentService,
      deliveryService,
      notificationService,
      reviewService,
    ],
  });
});
