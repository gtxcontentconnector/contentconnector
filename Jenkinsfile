// The GIT repository for this pipeline lib is defined in the global Jenkins setting
@Library('jenkins-pipeline-library')
import com.gentics.*

// Make the helpers aware of this jobs environment
JobContext.set(this)



final def gitCommitTag = '[Jenkins | ' + env.JOB_BASE_NAME + ']';

pipeline {
	agent {
		kubernetes {
			label env.BUILD_TAG
			defaultContainer 'build'
			yaml ocpWorker("""
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    jenkins_worker: true
  containers:
  - name: build
    image: """ + buildEnvironmentDockerImage("build/Dockerfile") + """
    imagePullPolicy: Always
    command:
    - cat
    tty: true
    resources:
      requests:
        cpu: '0'
        memory: '0'
      limits:
        cpu: '0'
        memory: '0'
  imagePullSecrets:
  - name: docker-jenkinsbuilds-apa-it
""")
		}
	}

	parameters {
		booleanParam(name: 'release', defaultValue: false, description: "Whether to release.")
	}

	options {
		withCredentials([usernamePassword(credentialsId: 'repo.gentics.com', usernameVariable: 'repoUsername', passwordVariable: 'repoPassword')])
		timestamps()
		timeout(time: 4, unit: 'HOURS')
		ansiColor('xterm')
    }

	stages {
		stage('Maven boild') {
			steps {
				sshagent(['git']) {
					script {
						if (Boolean.valueOf(params.release)) {
							sh "mvn -B release:prepare release:perform -Dresume=false -DignoreSnapshots=true -Darguments=\"-DskipTests\""
						} else {
							sh "mvn -B clean test -Dmaven.test.failure.ignore"
						}
					}
				}
			}

			post {
				always {
					script {
						if (!Boolean.valueOf(params.release)) {
							junit  "**/target/surefire-reports/*.xml"
						}
					}
				}
			}
		}
	}

	post {
		always {
			notifyMattermostUsers()
		}
	}
}
