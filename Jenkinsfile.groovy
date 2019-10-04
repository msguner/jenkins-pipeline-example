def mvnHome

pipeline {
    agent any

    stages {
        stage('Initialize') {
            steps {
                git 'https://github.com/msguner/jenkins-pipeline-example.git'
                checkout scm
                mvnHome = tool 'maven3.6.2'
            }
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
}