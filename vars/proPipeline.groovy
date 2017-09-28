import com.codependent.jenkins.pipelines.openshift.Utils

def call(String area, String project){  
  def pom
  def utils = new Utils()

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
          sh "mvn clean install"
        }
      }
      stage ('Acp Stage') {
        steps {
          echo 'Building & Deploying Docker Image'
          openshiftBuild(namespace: area+'-acp', bldCfg: project, showBuildLogs: 'true', env: [['ARTIFACT_VERSION' : pom.version]])
          echo 'Verifying deployment'
          openshiftVerifyDeployment(namespace: area+'-acp', depCfg: project)
          echo 'Tagging immage'
          openshiftTag(namespace: area+'-acp', srcStream: project, srcTag: 'latest', destStream: project, destTag: pom.version)
        }
      }
      stage ('Uat Stage') {
        steps {
          script{
            utils.promoteAndVerify project, pom.version, 'promote-uat', area+'-acp', area+'-uat'
          } 
        }
      }
      stage ('Pro Stage') {
        steps {
          script{
            timeout(time:1, unit:'DAYS') {
              input message: 'Are you sure you want to deploy to Production?'
            }
            utils.promoteAndVerify project, pom.version, 'promote-pro', area+'-acp', area+'-pro'
          }
        }
      }
    }
  }
}