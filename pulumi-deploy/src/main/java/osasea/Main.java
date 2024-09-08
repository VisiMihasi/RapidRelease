package osasea;

import com.pulumi.Pulumi;
import com.pulumi.kubernetes.apps.v1.Deployment;
import com.pulumi.kubernetes.apps.v1.DeploymentArgs;
import com.pulumi.kubernetes.apps.v1.inputs.DeploymentSpecArgs;
import com.pulumi.kubernetes.core.v1.Service;
import com.pulumi.kubernetes.core.v1.ServiceArgs;
import com.pulumi.kubernetes.core.v1.inputs.*;
import com.pulumi.kubernetes.meta.v1.inputs.LabelSelectorArgs;
import com.pulumi.kubernetes.meta.v1.inputs.ObjectMetaArgs;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Pulumi.run(ctx -> {

            // MySQL Deployment
            var mysqlDeployment = new Deployment("mysqlDeployment", DeploymentArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("mysql-deployment")
                            .build())
                    .spec(DeploymentSpecArgs.builder()
                            .replicas(1)
                            .selector(LabelSelectorArgs.builder()
                                    .matchLabels(Map.of("app", "mysql-db"))
                                    .build())
                            .template(PodTemplateSpecArgs.builder()
                                    .metadata(ObjectMetaArgs.builder()
                                            .labels(Map.of("app", "mysql-db"))
                                            .build())
                                    .spec(PodSpecArgs.builder()
                                            .containers(ContainerArgs.builder()
                                                    .name("mysql-db")
                                                    .image("mysql:8.0")
                                                    .env(
                                                            EnvVarArgs.builder()
                                                                    .name("MYSQL_DATABASE")
                                                                    .value("osasea")
                                                                    .build(),
                                                            EnvVarArgs.builder()
                                                                    .name("MYSQL_ROOT_PASSWORD")
                                                                    .value("root")
                                                                    .build()
                                                    )
                                                    .ports(ContainerPortArgs.builder()
                                                            .containerPort(3306)
                                                            .build())
                                                    .resources(ResourceRequirementsArgs.builder()
                                                            .requests(Map.of("cpu", "100m", "memory", "256Mi"))
                                                            .limits(Map.of("cpu", "500m", "memory", "512Mi"))
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());

            // MySQL Service
            var mysqlService = new Service("mysqlService", ServiceArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("mysql-service")
                            .build())
                    .spec(ServiceSpecArgs.builder()
                            .selector(Map.of("app", "mysql-db"))
                            .ports(ServicePortArgs.builder()
                                    .port(3306)
                                    .targetPort(3306)
                                    .build())
                            .build())
                    .build());

            // RapidRelease App Deployment
            var appDeployment = new Deployment("appDeployment", DeploymentArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("rapidrelease-app-deployment")
                            .build())
                    .spec(DeploymentSpecArgs.builder()
                            .replicas(2)
                            .selector(LabelSelectorArgs.builder()
                                    .matchLabels(Map.of("app", "rapidrelease-app"))
                                    .build())
                            .template(PodTemplateSpecArgs.builder()
                                    .metadata(ObjectMetaArgs.builder()
                                            .labels(Map.of("app", "rapidrelease-app"))
                                            .build())
                                    .spec(PodSpecArgs.builder()
                                            .containers(ContainerArgs.builder()
                                                    .name("rapidrelease-app")
                                                    .image("visimihasi/ossasea-app:v2")
                                                    .env(
                                                            EnvVarArgs.builder()
                                                                    .name("SPRING_DATASOURCE_URL")
                                                                    .value("jdbc:mysql://mysql-service:3306/osasea")
                                                                    .build(),
                                                            EnvVarArgs.builder()
                                                                    .name("SPRING_DATASOURCE_USERNAME")
                                                                    .value("root")
                                                                    .build(),
                                                            EnvVarArgs.builder()
                                                                    .name("SPRING_DATASOURCE_PASSWORD")
                                                                    .value("root")
                                                                    .build()
                                                    )
                                                    .ports(ContainerPortArgs.builder()
                                                            .containerPort(8081)
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());

            // RapidRelease App Service 1
            var service1 = new Service("serviceNode1", ServiceArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("rapidrelease-service-node-1")
                            .build())
                    .spec(ServiceSpecArgs.builder()
                            .selector(Map.of("app", "rapidrelease-app"))
                            .ports(ServicePortArgs.builder()
                                    .port(8081)
                                    .targetPort(8081)
                                    .nodePort(30100)
                                    .build())
                            .type("NodePort")
                            .build())
                    .build());

            // RapidRelease App Service 2
            var service2 = new Service("serviceNode2", ServiceArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("rapidrelease-service-node-2")
                            .build())
                    .spec(ServiceSpecArgs.builder()
                            .selector(Map.of("app", "rapidrelease-app"))
                            .ports(ServicePortArgs.builder()
                                    .port(8081)
                                    .targetPort(8081)
                                    .nodePort(30110)
                                    .build())
                            .type("NodePort")
                            .build())
                    .build());
        });
    }
}