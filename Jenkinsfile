@Library('jenkins-pipeline-library') import com.gentics.*
JobContext.set(this)

properties([
        parameters([
                booleanParam(name: 'release',            defaultValue: false,  description: "Whether to invoke the release"),
        ])
])

node('jenkins-slave') {

	try {
		stage('Checkout') {
		    checkout scm
		}

		stage('Maven Build') {
			echo "Building " + env.BRANCH_NAME
			sshagent(['git']) {
				try {
					if (Boolean.valueOf(params.release)) {
						sh "mvn -B release:prepare release:perform -Dresume=false -DignoreSnapshots=true -Darguments=\"-DskipTests\""
					} else {
						sh "mvn -B clean test -Dmaven.test.failure.ignore"
					}
				} finally {
					step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/*.xml'])
				}
			}
		}
	} finally {
		notifyMattermostUsers()
	}
}
