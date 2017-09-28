def call(String namespace, String project){
  def pom

  pipeline {
    agent any
    tools { 
      maven 'M3' 
      jdk 'JDK8' 
    }
    stages {
      stage ('Commit Stage') {
        steps {
          echo 'Building application'
          script {
            pom = readMavenPom file: 'pom.xml'
          }
          sh '''
             echo "PATH = ${PATH}"
             echo "M2_HOME = ${M2_HOME}"
             ''' 
          sh "mvn clean install -U -Dmaven.test.failure.ignore=true"
        }
      }
      stage ('Dev Stage') {
        steps {
          echo 'Building & Deploying Docker Image'
          openshiftBuild(namespace: namespace, bldCfg: project, showBuildLogs: 'true', env: [ [ name: 'ARTIFACT_VERSION' , value: pom.version] ])
          echo 'Verifying deployment'
          openshiftVerifyDeployment(namespace: namespace, depCfg: project)
        }
      }
    }
  }
}