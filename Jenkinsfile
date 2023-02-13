node {

    stage('Clone Repository'){
            checkout scm
    }

    sh "git rev-parse HEAD > .git/commit-id"
    def commit_id = readFile('.git/commit-id').trim()

    stage('Build Jar') {
    		sh 'echo $PATH'
            sh '/opt/maven/bin/mvn -B -DskipTests clean package'
    }

    stage('Build Custom Image and Publish Image to Registry'){
        sh 'echo $PATH'
        def customImage = docker.build("${env.DOCKER_REPO}")
        docker.withRegistry('https://registry.hub.docker.com', 'docker-credentials') {
            customImage.push("${commit_id}")
            customImage.push("latest")
        }
    }

    stage('Get Latest Release of Helm Chart and unzip'){
         withCredentials([string(credentialsId: 'GH_TOKEN', variable: 'GH_TOKEN')])
         {
        sh"""
        rm -f *tar.gz
        echo ${env.GIT_HELM_REPO}
        export TAG=`eval curl -s -u $GH_TOKEN:x-oauth-basic https://api.github.com/repos/${env.GIT_HELM_REPO}/releases/latest | grep 'tag_name' | cut -d '\"' -f 4`
        echo \$TAG
        `curl -u $GH_TOKEN:x-oauth-basic https://github.com/${env.GIT_HELM_REPO}/archive/refs/tags/\$TAG.tar.gz -LJOH 'Accept: application/octet-stream'`
        ls -lrt
        tar -xvf *.tar.gz
        ls -lrt
        rm -f *tar.gz
        ls -lrt
        """
        }
    }

    stage ('Deploy or Upgrade Helm Chart for Todo Webapp') {
        withCredentials([string(credentialsId: 'jenkins-aws-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                         string(credentialsId: 'jenkins-aws-secret-key', variable: 'AWS_SECRET_ACCESS_KEY'),
                         string(credentialsId: 'db-password', variable: 'DB_PASSWORD')])
                         {
        sh"""
        export AWS_DEFAULT_REGION=${env.AWS_DEFAULT_REGION}
        export KOPS_STATE_STORE=${env.KOPS_STATE_STORE}
        kops export kubeconfig ${env.CLUSTER_NAME} --state ${env.KOPS_STATE_STORE} --admin
        helm upgrade --install --wait --set image.repository=${env.DOCKER_REPO},image.tag=${commit_id},configVariables.dbHost=${env.DB_HOST},configVariables.dbURL=${env.DB_URL},secrets.dbPassword=${DB_PASSWORD} webapp ./helm-chart*/webapp
        """
        }
    }
}