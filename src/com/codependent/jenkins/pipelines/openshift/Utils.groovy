package com.codependent.jenkins.pipelines.openshift

static def promoteAndVerify(project, sourceTag, destinationTag, sourceNamespace, destinationNamespace){
  echo "Tagging"
  openshiftTag(srcStream: project, srcTag: sourceTag, destStream: project, destTag: destinationTag, namespace: sourceNamespace)
  echo 'Verifying deployment'
  openshiftVerifyDeployment(namespace: destinationNamespace, depCfg: project)
}