import com.sun.codemodel.internal.JForEach

@NonCPS
def createMultipleChoiceParameter(String desc, String value) {
    return [$class: 'BooleanParameterDefinition', defaultValue: true, description: desc, name: value]
}

// Parametre olarak verilen arrayin eleman sayısı kadar çoktan seçmeli parametre oluşturur.
@NonCPS
def createMultipleChoiceParameters(array) {
    def createdParams = []
    for (int i = 0; i < array.size(); i++) {
        createdParams << createMultipleChoiceParameter('', array[i])
    }
    return createdParams
}

// Parametre olarak aldıgımız ve choice parameterdan gelen x:true,x:false formatındaki elementler parse edilir
@NonCPS
def getSelectedItems(items) {
    def selectedItems = []

    println "********" + items

    for (int i = 0; i < items.size(); i++) {
        def splitedItem = items[i].toString().split(':')
        def itemName = splitedItem[0]
        boolean itemSelect = splitedItem[1].toBoolean() //true or false

        println "splitedItem : " + splitedItem
        println "itemName : " + itemName + " --- itemSelect : " + itemSelect

        if (itemSelect) {
            selectedItems << itemName
        }
    }

    return selectedItems
}

node {
    def mvnHome
    def selectedFeature
    def selectedTags

    stage('Initialize') {
        git 'https://github.com/msguner/jenkins-pipeline-example.git'
        checkout scm
        mvnHome = tool 'maven3.6.2'
    }

//    stage('user interactive inputs') {
//        input id: 'User_input_id', message: 'Please select feature and tags', ok: 'Devam', parameters: [[$class: 'ChoiceParameter', choiceType: 'PT_SINGLE_SELECT', description: '', filterLength: 1, filterable: false, name: 'feature_param', randomName: 'choice-parameter-11912031710581742', script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: false, script: 'return ["Get feature script error"]'], script: [classpath: [], sandbox: false, script: 'return [\'test1.feature\',\'test2.feature\']']]], [$class: 'CascadeChoiceParameter', choiceType: 'PT_CHECKBOX', description: '', filterLength: 1, filterable: false, name: 'tags_param', randomName: 'choice-parameter-11912031712931182', referencedParameters: 'feature_param', script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: false, script: 'return ["Get tags script error"]'], script: [classpath: [], sandbox: false, script: 'return (feature_param=="test1.feature") ? [\'@scenario1_1\'] : [\'@scenario2_1\',\'@scenario2_2\']']]]]
//    }

    stage('Select feature') {
        final foundFiles = findFiles(glob: "src/test/java/myTests/**/*.feature")
        def features = []
        for (int i = 0; i < foundFiles.length; i++) {
            def filename = foundFiles[i].name
            features << filename
        }
        println("***** Features : " + features)

        selectedFeature = input(id: 'selectedFeature_input', message: 'Please select features',
                parameters: [[$class: 'ChoiceParameterDefinition', choices: features, name: 'feature_input']]
        )
    }

    stage("Select tags") {
        def folder = "${env.WORKSPACE}/src/test/java/myTests"

        def file = new File("${folder}/${selectedFeature}")
        def lines = file as String[]
        def tags = lines.findAll { it.trim().startsWith('@') }.collect {
            it.replaceAll("\\s", "")
        }
        println("*** : " + selectedFeature + " tags : " + tags)

        selectedTags = input(id: 'selectedTags_input',
                message: 'Select options',
                parameters: createMultipleChoiceParameters(tags)
        )

        //selected olan tagler alınır
        selectedTags = getSelectedItems(selectedTags)

        println("***** SelectedTags : " + selectedTags)
    }

    stage('Run karate tests') {
        selectedTags_mvn = selectedTags.join(",")

        // Run the maven build
        withEnv(["MVN_HOME=${mvnHome}"]) {
            if (isUnix()) {
                def myCommand = "$MVN_HOME/bin/mvn clean test -Dtest=TestRunner '-Dkarate.options=--tags ${selectedTags_mvn} classpath:myTests/${selectedFeature}'"
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