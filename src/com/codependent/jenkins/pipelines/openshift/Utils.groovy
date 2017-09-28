package com.codependent.jenkins.pipelines.openshift

def promoteAndVerify(project, sourceTag, destinationTag, sourceNamespace, destinationNamespace){
  echo "Tagging Image $project:$sourceTag -> $project:$destinationTag - $sourceNamespace"
  openshiftTag(srcStream: project, srcTag: sourceTag, destStream: project, destTag: destinationTag, namespace: sourceNamespace)
  echo 'Verifying deployment'
  openshiftVerifyDeployment(namespace: destinationNamespace, depCfg: project)
}