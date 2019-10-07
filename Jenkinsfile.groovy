import com.sun.codemodel.internal.JForEach

@NonCPS
def createMultipleChoiceParameter(String desc, String value) {
    return [$class: 'BooleanParameterDefinition', defaultValue: true, description: desc, name: value]
}

//Parametre olarak verilen arrayin eleman sayısı kadar çoktan seçmeli parametre oluşturur.
def createMultipleChoiceParameters(String[] array) {
    println("*** createMultipleChoiceParameters : " + array)

    def createdParams = []
    for (int i = 0; i < array.length; i++) {
        createdParams << createMultipleChoiceParameter(array[i], array[i])
    }
    return createdParams
}

node {
    def mvnHome

    stage('Initialize') {
        git 'https://github.com/msguner/jenkins-pipeline-example.git'
        checkout scm
        mvnHome = tool 'maven3.6.2'
    }

//    stage('user interactive inputs') {
//        input id: 'User_input_id', message: 'Please select feature and tags', ok: 'Devam', parameters: [[$class: 'ChoiceParameter', choiceType: 'PT_SINGLE_SELECT', description: '', filterLength: 1, filterable: false, name: 'feature_param', randomName: 'choice-parameter-11912031710581742', script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: false, script: 'return ["Get feature script error"]'], script: [classpath: [], sandbox: false, script: 'return [\'test1.feature\',\'test2.feature\']']]], [$class: 'CascadeChoiceParameter', choiceType: 'PT_CHECKBOX', description: '', filterLength: 1, filterable: false, name: 'tags_param', randomName: 'choice-parameter-11912031712931182', referencedParameters: 'feature_param', script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: false, script: 'return ["Get tags script error"]'], script: [classpath: [], sandbox: false, script: 'return (feature_param=="test1.feature") ? [\'@scenario1_1\'] : [\'@scenario2_1\',\'@scenario2_2\']']]]]
//    }

    def selectedFeature
    stage('Select feature') {
        final foundFiles = findFiles(glob: "src/test/java/myTests/**/*.feature")
        def features = []
        for (int i = 0; i < foundFiles.length; i++) {
            def filename = foundFiles[i].name
            features << filename
        }

        println("***** Features : " + features)

        selectedFeature = input(id: 'selectedFeature', message: 'Please select features',
                parameters: [[$class: 'ChoiceParameterDefinition', choices: features, name: 'feature_input']]
        )
    }

    def selectedTags
    stage("Select tags") {
        def folder = "${env.WORKSPACE}/src/test/java/myTests"

        def file = new File("${folder}/${selectedFeature}")
        def lines = file as String[]
        def tags = lines.findAll { it.trim().startsWith('@') }.collect {
            it.replaceAll("\\s", "")
        }

        println("*** : " + selectedFeature + " tags : " + tags)

//        selectedTags = tags

        /*
        def selectedTags = input(id: 'feature_input', message: 'Please select test tags for run).', parameters: [
                createBooleanParameter('Tag1', tags[0]),
                createBooleanParameter('ScenarioB', tags[1]),
        ])
        */

        selectedTags = input(id: 'chooseOptions',
                message: 'Select options',
                parameters: createMultipleChoiceParameters(tags)
//                [
//                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: tags[0]],
//                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: tags[1]],
//                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: tags[2]],
//                        [$class: 'BooleanParameterDefinition', defaultValue: true, description: '', name: tags[3]]
//                ]
        )

        println("***** SelectedTags : " + selectedTags)
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