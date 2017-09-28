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
          openshiftTag(srcStream: project, srcTag: 'latest', destStream: project, destTag: pom.version, namespace: area+'-acp')
        }
      }
      stage ('Uat Stage') {
        steps {
          echo 'Promoting Image from Acp'
          openshiftTag(srcStream: project, srcTag: pom.version, destStream: project, destTag: 'promote-uat', namespace: area+'-acp')
          echo 'Verifying deployment'
          openshiftVerifyDeployment(namespace: area+'-uat', depCfg: project)
        }
      }
      stage ('Pro Stage') {
        steps {
          script{
            timeout(time:1, unit:'DAYS') {
              input message: 'Are you sure you want to deploy to Production?'
            }
          }
          echo 'Promoting Image from Acp'
          openshiftTag(srcStream: project, srcTag: pom.version, destStream: project, destTag: 'promote-pro', namespace: area+'-acp')
          echo 'Verifying deployment'
          openshiftVerifyDeployment(namespace: area+'-pro', depCfg: project)
        }
      }
    }
  }
}