#!/usr/bin/groovy
@Library(['pichm-java-jnk']) _
bootPipeline([node_qualifier:'JDK21', mvn_version:'MVN_3_8', jdk_version:'JDK21']) {
    email = 'adil.derhali@prestataire.sihm.fr'
    auto_deploy_branch_list = ['main']
    deployment_env_list = ['INT1']
    application_name = 'SIMTAR'
    package_list = [appPackageName:'symphonie-conseil-simtar-tarifer-assurance-emprunteur-rs-api BACK', cfgPackageName:'symphonie-conseil-simtar-tarifer-assurance-emprunteur-rs-api BACK CFG']
}
