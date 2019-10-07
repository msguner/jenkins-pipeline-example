node {
    def mvnHome

    stage('Initialize') {
        git 'https://github.com/msguner/jenkins-pipeline-example.git'
        checkout scm
        mvnHome = tool 'maven3.6.2'
    }

    stage('Select feature and tags') {
        input id: 'User_input', message: 'Please select feature and tags', ok: 'Bir sonraki adıma geç',
                parameters: [[$class: 'ChoiceParameter', choiceType: 'PT_SINGLE_SELECT', description: '', filterLength: 1, filterable: false, name: 'feature_param', randomName: 'choice-parameter-11674263150629534', script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: false, script: 'return ["Get feature script error"]'], script: [classpath: [], sandbox: false, script: 'return [\'test1.feature\',\'test2.feature\']']]], [$class: 'CascadeChoiceParameter', choiceType: 'PT_MULTI_SELECT', description: '', filterLength: 1, filterable: false, name: 'tags_param', randomName: 'choice-parameter-11674263152945020', referencedParameters: 'feature_param', script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: false, script: 'return ["Get tags script error"]'], script: [classpath: [], sandbox: false, script: 'return (feature_param=="test1.feature") ? [\'@scenario1_1\'] : [\'@scenario2_1\',\'@scenario2_1\']']]]]
    }

    stage('Run karate tests') {
        def tags1 = ["scenario1", "@test1_scenario2", "test1_scenario3", "test1_scenario4"].join(",")
        def tags2 = ["scenario1", "@test2_scenario2"].join(",")
        def features = ["test1.feature", "test2.feature"]

        // Run the maven build
        withEnv(["MVN_HOME=${mvnHome}"]) {
            if (isUnix()) {
                def myCommand = "$MVN_HOME/bin/mvn clean test -Dtest=TestRunner '-Dkarate.options=--tags ${tags1} classpath:myTests/test1.feature'"
                sh(myCommand)
//                    def myCommand2 = "$MVN_HOME/bin/mvn clean test -Dtest=TestRunner '-Dkarate.options=--tags ${tags2} classpath:myTests/test2.feature'"
//                    sh(myCommand2)
            } else {
                bat(/"%MVN_HOME%\bin\mvn" -Dtest=TestRunner/)
            }
        }
    }

    stage('Generate cucumber report') {
        cucumber failedFeaturesNumber: -1, failedScenariosNumber: -1, failedStepsNumber: -1, fileIncludePattern: '**/*.json', jsonReportDirectory: "${env.WORKSPACE}/target/surefire-reports/", pendingStepsNumber: -1, skippedStepsNumber: -1, sortingMethod: 'ALPHABETICAL', undefinedStepsNumber: -1
    }
}