import com.codependent.jenkins.pipelines.openshift.Utils

def call(String namespace, String project){
  def processedMavenGoals = mavenGoals.join ' '
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
            def utils = new Utils()
            utils.hello 'Codependent'
          }
          sh '''
             echo "PATH = ${PATH}"
             echo "M2_HOME = ${M2_HOME}"
             ''' 
          sh "mvn clean deploy -U -Dmaven.test.failure.ignore=true"
        }
      }
      stage ('Publish Dev') {
        steps {
          echo 'Publishing in Dev'
          openshiftBuild(namespace: namespace, bldCfg: project, showBuildLogs: 'true')
          openshiftVerifyDeployment(namespace: namespace, depCfg: project)
        }
      }
    }
  }
}