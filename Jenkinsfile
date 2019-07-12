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
			yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    jenkinsbuild: true
spec:
  volumes:
  - name: cache
    hostPath:
      path: /opt/kubernetes/cache
  nodeSelector:
    jenkins_worker: true
  affinity:
    podAntiAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        podAffinityTerm:
          labelSelector:
            matchExpressions:
            - key: jenkinsbuild
              operator: In
              values:
              - true
          topologyKey: kubernetes.io/hostname
  containers:
  - name: build
    image: """ + buildEnvironmentDockerImage("build/Dockerfile") + """
    imagePullPolicy: Always
    command:
    - cat
    tty: true
    resources:
      requests:
        cpu: 3
        memory: 4Gi
    volumeMounts:
    - mountPath: /home/jenkins/.m2/repository
      name: cache
      subPath: maven
  imagePullSecrets:
  - name: docker-jenkinsbuilds-apa-it
"""
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
					junit  "**/target/surefire-reports/*.xml"
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
