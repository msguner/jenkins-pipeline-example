pipeline {
  agent any
  stages {
    stage('Initialize') {
      steps {
        echo 'selam kankiler'
      }
    }
    stage('User inputs') {
      steps {
        input id: 'Feature_input_id', message: 'Please select feature file', ok: 'asdasd', parameters: [[$class: 'ChoiceParameter', choiceType: 'PT_SINGLE_SELECT', description: '', filterLength: 1, filterable: false, name: 'feature_input_name', randomName: 'choice-parameter-11574857701447962', script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: false, script: ''], script: [classpath: [], sandbox: false, 
        script: '''import groovy.io.FileType

def fileList = []

def dir = new File("/var/lib/jenkins/workspace/jenkins-pipeline-example/src/test/java/tests")
dir.eachFileRecurse (FileType.FILES) { file ->
  fileList << file
}

return fileList''']]]]
      }
    }
  }
}
