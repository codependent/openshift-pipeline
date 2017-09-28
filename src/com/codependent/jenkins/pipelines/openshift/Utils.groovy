package com.codependent.jenkins.pipelines.openshift

def promoteAndVerify(project, sourceTag, destinationTag, imageNamespace, deploymentNamespace){
  echo "Tagging Image $project:$sourceTag -> $project:$destinationTag in mamespace $imageNamespace"
  openshiftTag(srcStream: project, srcTag: sourceTag, destStream: project, destTag: destinationTag, namespace: imageNamespace)
  echo 'Verifying deployment'
  openshiftVerifyDeployment(namespace: deploymentNamespace, depCfg: project)
}