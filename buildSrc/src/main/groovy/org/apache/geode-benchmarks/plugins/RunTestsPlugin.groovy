package org.apache.geode.plugins

import org.gradle.api.*
import org.hidetake.groovy.ssh.Ssh

class RunTestsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.task("runTests") {
            doLast {
                def repoUrl = 'https://github.com/apache/geode'
                if (project.hasProperty('repo')) {
                    repoUrl = project.findProperty('repo')
                    if (!repoUrl.startsWith('https:')) {
                        repoUrl = 'https://github.com/' + repoUrl
                    }
                }

                def branch = 'develop'
                if (project.hasProperty("branch")) {
                    branch = project.findProperty("branch")
                }


                if (!project.hasProperty("tag")) {
                    throw new IllegalArgumentException("no tag provided")
                }
                def tag = project.findProperty("tag")

                def machineName = project.findProperty("machineName")
                def ssh = Ssh.newService()
                ssh.remotes.create('locator') {
                    host = machineName
                    user = 'geode'
                    identity = new File("${System.properties['user.home']}/.geode-benchmarks/${tag}-privkey.pem")
                    knownHosts = allowAnyHosts
                }

                ssh.run {
                    session(ssh.remotes['locator']) {
                        execute 'rm -rf geode'
                        execute 'git clone ' + repoUrl
                        execute 'cd geode && git checkout ' + branch
                    }


                }

            }
        }
    }
}
