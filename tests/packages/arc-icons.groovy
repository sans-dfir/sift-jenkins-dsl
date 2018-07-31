
pipelineJob('sift.packages.arc-icons') {
  definition {
    cps {
      sandbox()
      script("""
        pipeline {
            agent none
            stages {
                stage('Clone') {
                    steps {
                        node('docker') {
                            git "https://github.com/sans-dfir/sift-saltstack.git"
                        }
                    }
                }
                stage('Fetch Docker Testing Image') {
                    steps {
                        node('docker') {
                            container('dind') {
                                sh "docker pull sansdfir/sift-salt-tester"
                            }
                        }
                    }
                }
                stage('Test State') {
                    steps {
                        node('docker') {
                            container('dind') {
                                sh "docker run --rm --cap-add SYS_ADMIN -v `pwd`/sift:/srv/salt/sift sansdfir/sift-salt-tester salt-call --local --retcode-passthrough --state-output=mixed state.sls sift.packages.arc-icons"
                            }
                        }
                    }
                }
            }
        }
      """.stripIndent())
    }
  }

  logRotator {
      numToKeep(100)
      daysToKeep(15)
  }

  publishers {
      extendedEmail {
          recipientList('$DEFAULT_RECIPIENTS')
          contentType('text/plain')
          triggers {
              stillFailing {
                  attachBuildLog(true)
              }
          }
      }

      wsCleanup()
  }
}