package osasea;

import com.pulumi.Pulumi;
import com.pulumi.command.local.Command;
import com.pulumi.command.local.CommandArgs;
import com.pulumi.kubernetes.apps.v1.Deployment;
import com.pulumi.kubernetes.apps.v1.DeploymentArgs;
import com.pulumi.kubernetes.apps.v1.inputs.DeploymentSpecArgs;
import com.pulumi.kubernetes.batch.v1.CronJob;
import com.pulumi.kubernetes.batch.v1.CronJobArgs;
import com.pulumi.kubernetes.batch.v1.inputs.CronJobSpecArgs;
import com.pulumi.kubernetes.batch.v1.inputs.JobTemplateSpecArgs;
import com.pulumi.kubernetes.batch.v1.inputs.JobSpecArgs;
import com.pulumi.kubernetes.core.v1.Service;
import com.pulumi.kubernetes.core.v1.ServiceArgs;
import com.pulumi.kubernetes.core.v1.inputs.*;
import com.pulumi.kubernetes.meta.v1.inputs.LabelSelectorArgs;
import com.pulumi.kubernetes.meta.v1.inputs.ObjectMetaArgs;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Pulumi.run(ctx -> {

            var kindCluster = new Command("kind-cluster", CommandArgs.builder()
                    .create("kind create cluster --name kind-cluster --config=./kind-config.yaml")
                    .build());

            // MySQL Deployment on specific node (node affinity)
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
                                            .affinity(AffinityArgs.builder()
                                                    .nodeAffinity(NodeAffinityArgs.builder()
                                                            .requiredDuringSchedulingIgnoredDuringExecution(
                                                                    NodeSelectorArgs.builder()
                                                                            .nodeSelectorTerms(
                                                                                    NodeSelectorTermArgs.builder()
                                                                                            .matchExpressions(
                                                                                                    NodeSelectorRequirementArgs.builder()
                                                                                                            .key("kubernetes.io/hostname")
                                                                                                            .operator("In")
                                                                                                            .values("rapid-release-worker")
                                                                                                            .build()
                                                                                            )
                                                                                            .build()
                                                                            )
                                                                            .build()
                                                            )
                                                            .build()
                                                    )
                                                    .build()
                                            )
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
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());

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

            // App Deployment with node affinity and imagePullPolicy set to Always
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
                                            .affinity(AffinityArgs.builder()
                                                    .nodeAffinity(NodeAffinityArgs.builder()
                                                            .preferredDuringSchedulingIgnoredDuringExecution(
                                                                    PreferredSchedulingTermArgs.builder()
                                                                            .preference(NodeSelectorTermArgs.builder()
                                                                                    .matchExpressions(
                                                                                            NodeSelectorRequirementArgs.builder()
                                                                                                    .key("kubernetes.io/hostname")
                                                                                                    .operator("In")
                                                                                                    .values("rapid-release-worker2")
                                                                                                    .build()
                                                                                    )
                                                                                    .build())
                                                                            .weight(1)
                                                                            .build(),
                                                                    PreferredSchedulingTermArgs.builder()
                                                                            .preference(NodeSelectorTermArgs.builder()
                                                                                    .matchExpressions(
                                                                                            NodeSelectorRequirementArgs.builder()
                                                                                                    .key("kubernetes.io/hostname")
                                                                                                    .operator("In")
                                                                                                    .values("rapid-release-worker")
                                                                                                    .build()
                                                                                    )
                                                                                    .build())
                                                                            .weight(1)
                                                                            .build()
                                                            )
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .containers(ContainerArgs.builder()
                                                    .name("rapidrelease-app")
                                                    .image("visimihasi/ossasea-app:latest")
                                                    .imagePullPolicy("Always")  // Pulls the latest image
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

            var serviceApp = new Service("rapidreleaseServiceApp", ServiceArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("rapidrelease-service-app")
                            .build())
                    .spec(ServiceSpecArgs.builder()
                            .selector(Map.of("app", "rapidrelease-app"))
                            .ports(ServicePortArgs.builder()
                                    .port(8081)
                                    .targetPort(8081)
                                    .build())
                            .build())
                    .build());

            var serviceNodePort = new Service("serviceNodePort", ServiceArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("rapidrelease-service-node")
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

            var cronJob1 = new CronJob("cronJob1", CronJobArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("app-restart-cronjob-worker1")
                            .build())
                    .spec(CronJobSpecArgs.builder()
                            .schedule("*/15 * * * *") // Every 15 minutes
                            .jobTemplate(JobTemplateSpecArgs.builder()
                                    .spec(JobSpecArgs.builder()
                                            .template(PodTemplateSpecArgs.builder()
                                                    .spec(PodSpecArgs.builder()
                                                            .containers(ContainerArgs.builder()
                                                                    .name("kubectl")
                                                                    .image("bitnami/kubectl")
                                                                    .command("/bin/sh")
                                                                    .args("-c", "kubectl rollout restart deployment/rapidrelease-app-deployment")
                                                                    .build())
                                                            .restartPolicy("OnFailure")
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());

            var cronJob2 = new CronJob("cronJob2", CronJobArgs.builder()
                    .metadata(ObjectMetaArgs.builder()
                            .name("app-restart-cronjob-worker2")
                            .build())
                    .spec(CronJobSpecArgs.builder()
                            .schedule("*/17 * * * *") // Every 17 minutes
                            .jobTemplate(JobTemplateSpecArgs.builder()
                                    .spec(JobSpecArgs.builder()
                                            .template(PodTemplateSpecArgs.builder()
                                                    .spec(PodSpecArgs.builder()
                                                            .containers(ContainerArgs.builder()
                                                                    .name("kubectl")
                                                                    .image("bitnami/kubectl")
                                                                    .command("/bin/sh")
                                                                    .args("-c", "kubectl rollout restart deployment/rapidrelease-app-deployment")
                                                                    .build())
                                                            .restartPolicy("OnFailure")
                                                            .build())
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build());
        });
    }
}