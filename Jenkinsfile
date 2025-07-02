
pipeline {
    agent {
        label 'Yocto'
    }

    environment {
        BASE_PATH = "/mnt/build" 
        NEW_DIR = "myhello"

        YOCTO_WORKSPACE   = "${BASE_PATH}/poky"
        POKY_DIR          = "${YOCTO_WORKSPACE}"
        BUILD_DIR         = "${POKY_DIR}/build"

        SSTATE_DIR_PATH = '/mnt/efs/fs/yocto-sstate'
        DL_DIR_PATH     = '/mnt/efs/fs/yocto-dl'
        TMP_DIR_PATH    = '/mnt/build/tmp'

        MACHINE         = 'qemux86-64'
        IMAGE           = 'core-image-minimal'

        BB_NUMBER_THREADS = '8'
        PARALLEL_MAKE     = '-j4'
    }

    options {
        timeout(time: 4, unit: 'HOURS')
        skipDefaultCheckout()
    }

    stages {
        stage('Prepare Yocto Workspace') {
            steps {
                script {
                    echo "Preparing Yocto workspace at ${YOCTO_WORKSPACE}"
                    sh """
                        echo "${YOCTO_WORKSPACE}"
                        sudo chown -R jenkins:jenkins /mnt/build/*
                        sudo chmod 755 /mnt/build/*
                        mkdir -p /mnt/efs/fs/yocto-dl 
                        mkdir -p /mnt/efs/fs/yocto-sstate
                        sudo chown -R jenkins:jenkins /mnt/efs/fs/yocto-dl/
                        sudo chown -R jenkins:jenkins /mnt/efs/fs/yocto-sstate/
                        sudo rm -rf "${BUILD_DIR}"
                    """
                }
            }
        }

        stage('Clone Repositories') {
            steps {
                script {
                    echo "Cloning Poky repository..."

                    dir("${YOCTO_WORKSPACE}") {
                        git branch: 'kirkstone', url: 'https://git.yoctoproject.org/git/poky'
                    } 
                    dir("${BASE_PATH}/${NEW_DIR}") { 
                        echo "Cloning meta-myhello layer..."
                        git branch: 'main', url: 'https://github.com/santoshPagire/yocto_new.git'
                        sh "cp -r meta-myhello/ ../poky"
                    }
                }
            }
        }

        stage('Configure Build Environment') {
            steps {
                script {

                    dir("${YOCTO_WORKSPACE}") {
                        sh '''
                            echo "$(date),$(ps -p $$ -o %cpu=,%mem=)" >> yocto_build.csv
                            bash -c '
                                pwd
                                source oe-init-build-env 
                            '
                        '''
                    }
                    sh """
                        echo "\\\$(date),\\\$(ps -p \\\$\$ -o %cpu=,%mem=)" >> yocto_os.csv
                        echo "# Setting MACHINE" >> ${BUILD_DIR}/conf/local.conf
                        echo 'MACHINE = "${MACHINE}"' >> ${BUILD_DIR}/conf/local.conf

                        echo "# Setting parallel build options" >> ${BUILD_DIR}/conf/local.conf
                        echo 'BB_NUMBER_THREADS = "${BB_NUMBER_THREADS}"' >> ${BUILD_DIR}/conf/local.conf
                        echo 'PARALLEL_MAKE = "${PARALLEL_MAKE}"' >> ${BUILD_DIR}/conf/local.conf

                        echo "# Setting shared state and download directories" >> ${BUILD_DIR}/conf/local.conf
                        echo 'SSTATE_DIR ?= "${SSTATE_DIR_PATH}"' >> ${BUILD_DIR}/conf/local.conf
                        echo 'DL_DIR ?= "${DL_DIR_PATH}"' >> ${BUILD_DIR}/conf/local.conf
                        echo 'TMPDIR = "${TMP_DIR_PATH}"' >> ${BUILD_DIR}/conf/local.conf

                        echo "# Adding custom packages" >> ${BUILD_DIR}/conf/local.conf
                        echo 'CORE_IMAGE_EXTRA_INSTALL += "hello-world"' >> ${BUILD_DIR}/conf/local.conf

                    """
                    sh """
                        echo "# Adding custom layers to bblayers.conf"
                        sed -i '/^BBLAYERS ?= /a \\  /mnt/build/poky/meta-myhello \\\\' /mnt/build/poky/build/conf/bblayers.conf
                    """
                }
            }
        }

        stage('Build Yocto Image') {
            steps {
                script {
                    echo "Starting BitBake for image: ${IMAGE}..."
                    dir("${YOCTO_WORKSPACE}") {
                        sh '''
                            bash -c '
                                pwd
                                source oe-init-build-env 
                                sudo chown
                                bitbake -c cleansstate perl-native
                                bitbake -c cleansstate openssl
                                bitbake ${IMAGE}
                            '
                        '''
                    }
                }
            }
        }

        // stage('Archive Build Artifacts') {
        //     steps {
        //         script {
        //             echo "Archiving generated images..."
        //             def deployDir = "${BUILD_DIR}/tmp/deploy/images/${MACHINE}"
        //             archiveArtifacts artifacts: "${deployDir}/**", fingerprint: true, allowEmptyArchive: false
        //             echo "Images archived from ${deployDir}"
        //         }
        //     }
        // }
    }

    post {
        always {
            echo "Yocto build pipeline finished."
            archiveArtifacts artifacts: '*.csv'
            cleanWs()
        }
        success {
            echo "Yocto image build SUCCEEDED!"

        }
        failure {
            echo "Yocto image build FAILED!"
        }
        unstable {
            echo "Yocto image build UNSTABLE (e.g., some tests failed)."
        }
    }
}
