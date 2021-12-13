def getDockerCompatibleTag(tag) {
  def fixedTag = tag.replaceAll('\\+', '-')
  return fixedTag
}

def getDockerImageName(folder) {
  files = findFiles(glob: "${DOCKERFILE_PATH}/*.*")
  def list = []

  for (def file : files) {
    def token = (file.getName()).tokenize('.')
    if (token.size() > 1 && token[1] == "Dockerfile") {
      list.push(token[0])
    } else {
      println("File not added " + token[0])
    }
  }
  return list
}

def getArtifactoryUrl() {
  echo "Choosing an Artifactory port based off of branch name: $GIT_BRANCH"

  if (GIT_BRANCH ==~ /release-.*/){
    echo "Publishing to 16003-RELEASE-LOCAL"
    return "artifactory.jpl.nasa.gov:16003"
  }
  else if (GIT_BRANCH ==~ /staging/) {
    echo "Publishing to 16002-STAGE-LOCAL"
    return "artifactory.jpl.nasa.gov:16002"
  }
  else {
    echo "Publishing to 16001-DEVELOP-LOCAL"
    return "artifactory.jpl.nasa.gov:16001"
  }
}

def getPublishPath() {
  if (GIT_BRANCH ==~ /release-.*/) {
    return "general/gov/nasa/jpl/aerie/"
  }
  else if (GIT_BRANCH ==~ /staging/) {
    return "general-stage/gov/nasa/jpl/aerie/"
  }
  else {
    return "general-develop/gov/nasa/jpl/aerie/"
  }
}

def getArtifactTag() {
  if (GIT_BRANCH ==~ /(develop|staging|release-.*)/) {
    return GIT_BRANCH
  } else {
    return GIT_COMMIT
  }
}

def getReleaseTag() {
  if (GIT_BRANCH ==~ /release-.*/) {
    String[] str = GIT_BRANCH.split('-');
    if (str.size() == 2) {
      return str[1];
    }
  }
  return ""
}

void setBuildStatus(String message, String state, String context) {
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: env.GIT_URL],
      commitShaSource: [$class: "ManuallyEnteredShaSource", sha: env.GIT_COMMIT],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: context],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [
        [$class: "AnyBuildResult", message: message, state: state.toUpperCase()] ] ]
  ]);
}

// Save built image name:tag
def buildImages = []

// Save dockerfile name inside scipt/Dockerfiles
def imageNames = []

pipeline {

  agent {
    label 'CAE-Jenkins2-DH-Agents-Linux'
  }

  environment {
    ARTIFACT_TAG = "${getArtifactTag()}"
    ARTIFACTORY_URL = "${getArtifactoryUrl()}"
    ARTIFACT_PATH = "${ARTIFACTORY_URL}/gov/nasa/jpl/aerie"
    AERIE_SECRET_ACCESS_KEY = credentials('Aerie-Access-Token')
    DOCKER_TAG = "${getDockerCompatibleTag(ARTIFACT_TAG)}"
    DOCKERFILE_PATH = "scripts/dockerfiles"
    DOCKERFILE_DIR = "${env.WORKSPACE}/${DOCKERFILE_PATH}"
  }

  stages {
    stage ('Setup') {
      steps {
        echo "Printing environment variables..."
        sh "env | sort"
      }
    }
    stage ('Enter Docker Container') {
      agent {
        docker {
          reuseNode true
          registryUrl 'https://artifactory.jpl.nasa.gov:16001'
          registryCredentialsId 'Artifactory-credential'
          image 'gov/nasa/jpl/aerie/jenkins/aerie:latest'
          alwaysPull true
          args '-u root --mount type=bind,source=${WORKSPACE},target=/home --workdir=/home -v /var/run/docker.sock:/var/run/docker.sock'
        }
      }
      stages {
        stage ('Build') {
          steps {
            script { setBuildStatus("Building", "pending", "jenkins/branch-check"); }
            echo "Building $ARTIFACT_TAG..."
            sh './gradlew classes'
          }
        }
        stage ('Test') {
          steps {
            script { setBuildStatus("Testing", "pending", "jenkins/branch-check"); }
            sh "./gradlew test"

              // Jenkins will complain about "old" test results if Gradle didn't need to re-run them.
              // Bump their last modified time to trick Jenkins.
              sh 'find . -name "TEST-*.xml" -exec touch {} \\;'

              junit testResults: '*/build/test-results/test/*.xml'
          }
        }
        stage ('Assemble') {
          steps {
            echo 'Publishing JARs and Aerie Docker Compose to Artifactory...'
            sh '''
            ASSEMBLE_PREP_DIR=$(mktemp -d)
            STAGING_DIR=$(mktemp -d)

            ./gradlew assemble

            # For mission models
            mkdir -p ${ASSEMBLE_PREP_DIR}/missionmodels
            cp banananation/build/libs/*.jar \
               ${ASSEMBLE_PREP_DIR}/missionmodels/

            # For services
            mkdir -p ${ASSEMBLE_PREP_DIR}/services
            cp merlin-server/build/distributions/*.tar \
               ${ASSEMBLE_PREP_DIR}/services/
            cp scheduler-server/build/distributions/*.tar \
               ${ASSEMBLE_PREP_DIR}/services/

            # For deployment
            cp -r ./deployment ${STAGING_DIR}
            tar -czf aerie-${ARTIFACT_TAG}.tar.gz -C ${ASSEMBLE_PREP_DIR}/ .
            tar -czf aerie-docker-compose.tar.gz -C ${STAGING_DIR}/ .
            rm -rfv ${ASSEMBLE_PREP_DIR}
            rm -rfv ${STAGING_DIR}
            '''
          }
        }
        stage ('Release') {
          when {
            expression { GIT_BRANCH ==~ /(develop|staging|release-.*)/ }
          }
          steps {
            script {
              try {
                def server = Artifactory.newServer url: 'https://artifactory.jpl.nasa.gov/artifactory', credentialsId: '9db65bd3-f8f0-4de0-b344-449ae2782b86'
                def uploadSpec =
                """
                {
                  "files": [
                    {
                      "pattern": "aerie-${ARTIFACT_TAG}.tar.gz",
                      "target": "${getPublishPath()}",
                      "recursive":false
                    },
                    {
                      "pattern": "aerie-docker-compose.tar.gz",
                      "target": "${getPublishPath()}",
                      "recursive":false
                    }
                  ]
                }
                """
                def buildInfo = server.upload spec: uploadSpec
                server.publishBuildInfo buildInfo
              } catch (Exception e) {
                  println("Publishing to Artifactory failed with exception: ${e.message}")
                  currentBuild.result = 'UNSTABLE'
              }
            }
          }
        }
        stage ('Docker') {
          when {
            expression { GIT_BRANCH ==~ /(develop|staging|release-.*)/ }
          }
          steps {
            script {
              imageNames = getDockerImageName(env.DOCKERFILE_DIR)
              docker.withRegistry("https://$ARTIFACTORY_URL", '9db65bd3-f8f0-4de0-b344-449ae2782b86') {
                for (def name: imageNames) {
                  def tag_name="$ARTIFACT_PATH/$name:$DOCKER_TAG"
                  def image = docker.build("${tag_name}", "--progress plain -f ${DOCKERFILE_PATH}/${name}.Dockerfile --rm ." )
                  image.push()
                  buildImages.push(tag_name)
                }
              }
            }
          }
        }
        stage ('Generate Javadoc for Merlin-SDK') {
          when {
            expression { GIT_BRANCH ==~ /(develop|staging|release-.*)/ }
          }
          steps {
            sh '''
              JAVADOC_PREP_DIR=$(mktemp -d)
              mkdir -p ${JAVADOC_PREP_DIR}/javadoc/framework
              mkdir -p ${JAVADOC_PREP_DIR}/javadoc/framework-junit
              mkdir -p ${JAVADOC_PREP_DIR}/javadoc/framework-processor
              mkdir -p ${JAVADOC_PREP_DIR}/javadoc/contrib

              ./gradlew merlin-framework:javadoc
              cp -rv merlin-framework/build/docs/javadoc/. ${JAVADOC_PREP_DIR}/javadoc/framework/

              ./gradlew merlin-framework-junit:javadoc
              cp -rv merlin-framework-junit/build/docs/javadoc/. ${JAVADOC_PREP_DIR}/javadoc/framework-junit/

              ./gradlew merlin-framework-processor:javadoc
              cp -rv merlin-framework-processor/build/docs/javadoc/. ${JAVADOC_PREP_DIR}/javadoc/framework-processor/

              ./gradlew contrib:javadoc
              cp -rv contrib/build/docs/javadoc/. ${JAVADOC_PREP_DIR}/javadoc/contrib/

              git checkout gh-pages
              rsync -av --delete ${JAVADOC_PREP_DIR}/javadoc/ javadoc
              rm -rf ${JAVADOC_PREP_DIR}

              git config user.email "achong@jpl.nasa.gov"
              git config user.name "Jenkins gh-pages sync"
              git add javadoc/
              git diff --quiet HEAD || git commit -m "Publish Javadocs for commit ${GIT_COMMIT}"
              git push https://${AERIE_SECRET_ACCESS_KEY}@github.jpl.nasa.gov/Aerie/aerie.git gh-pages
              git checkout ${GIT_BRANCH}
            '''
          }
        }
      }
      post {
        cleanup {
          cleanWs()
          deleteDir()
        }
      }
    }
    stage ('Trigger document generation') {
      when {
        expression { GIT_BRANCH ==~ /(release-.*)/ }
      }
      steps {
        script {
          String tag = getReleaseTag();
          build job: '../aerie-gh-pages', parameters:[
            string(name: 'BRANCH', value: "${GIT_BRANCH}"),
            string(name: 'TAG', value: "${tag}")
          ]
        }
      }
    }
  }
  post {
    always {
      script {
        println(buildImages)
        for(def image: buildImages) {
          def removeCmd = "docker rmi $image"
          sh removeCmd
        }
      }
      echo 'Cleaning up images'
      sh "docker image prune -f"

      echo 'Logging out docker'
      sh 'docker logout || true'

      setBuildStatus("Build ${currentBuild.currentResult}", "${currentBuild.currentResult}", "jenkins/branch-check")
    }
    unstable {
      emailext subject: "Jenkins UNSTABLE: ${env.JOB_BASE_NAME} #${env.BUILD_NUMBER}",
      body: """
          <p>Jenkins job unstable (failed tests): <br> <a href=\"${env.BUILD_URL}\">${env.JOB_NAME} #${env.BUILD_NUMBER}</a></p>
      """,
      mimeType: 'text/html',
      recipientProviders: [[$class: 'FailingTestSuspectsRecipientProvider']]
    }
    failure {
      emailext subject: "Jenkins FAILURE: ${env.JOB_BASE_NAME} #${env.BUILD_NUMBER}",
      body: """
          <p>Jenkins job failure: <br> <a href=\"${env.BUILD_URL}\">${env.JOB_NAME} #${env.BUILD_NUMBER}</a></p>
      """,
      mimeType: 'text/html',
      recipientProviders: [[$class: 'CulpritsRecipientProvider']]
    }
    cleanup {
      cleanWs()
      deleteDir()
    }
  }
}
