import com.codependent.jenkins.pipelines.openshift.Utils

def call(String area, String project){  
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
            def utils = new Utils()
            utils.hello 'Codependent'
          }
          sh '''
             echo "PATH = ${PATH}"
             echo "M2_HOME = ${M2_HOME}"
             ''' 
          sh "mvn clean install"
        }
      }
      stage ('Acp Stage') {
        steps {
          echo 'Building & Deploying Docker Image'
          openshiftBuild(namespace: area+'-acp', bldCfg: project, showBuildLogs: 'true')
          echo 'Verifying deployment'
          openshiftVerifyDeployment(namespace: area+'-acp', depCfg: project)
          echo 'Tagging immage'
          openshiftTag(srcStream: area+'-acp', srcTag: 'latest', destStream: area+'-acp', destTag: pom.version)
        }
      }
    }
  }
}