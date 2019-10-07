import com.sun.codemodel.internal.JForEach

@NonCPS
def createMultipleChoiceParameter(String desc, String value) {
    return [$class: 'BooleanParameterDefinition', defaultValue: true, description: desc, name: value]
}

node {
    def mvnHome

    stage('Initialize') {
        git 'https://github.com/msguner/jenkins-pipeline-example.git'
        checkout scm
        mvnHome = tool 'maven3.6.2'
    }

    def selectedFeature
    stage('Select feature') {
        final foundFiles = findFiles(glob: "src/test/java/tests/**/*.feature")
        def features = []
        for (int i = 0; i < foundFiles.length; i++) {
            def filename = foundFiles[i]
            features << filename
        }

        selectedFeature = input message: 'Please select features', parameters: [string(defaultValue: '', description: '', name: features)]
    }

    stage("Select tags") {
        def folder = "${env.WORKSPACE}/src/test/java/myTests"

        def file = new File("${folder}/${Feature}")
        def lines = file as String[]
        def tags = lines.findAll { it.trim().startsWith('@') }.collect {
            it.replaceAll("\\s", "")
        }

        println("*** tags : " + tags)

        /*
        def selectedTags = input(id: 'feature_input', message: 'Please select test tags for run).', parameters: [
                createBooleanParameter('Tag1', tags[0]),
                createBooleanParameter('ScenarioB', tags[1]),
        ])


        def selectedTags = input(id: 'chooseOptions',
                message: 'Select options',
                parameters: [
                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Option A'],
                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Option B'],
                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: 'Option C']
                ]
        )
        */
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