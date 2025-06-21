template = 
'''
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: terraform
  name: terraform
spec:
  containers:
  - command:
    - sleep
    - "3600"
    image: hashicorp/terraform
    name: terraform
'''

properties([parameters([choice(choices: ['apply', 'destroy'], description: 'actions', name: 'action')])])

podTemplate(cloud: 'kubernetes', label: 'terraform', yaml: template) {
    node ("terraform") {
        container ("terraform"){
            stage ("Checkout SCM") {
                git branch: 'main', url: 'https://github.com/aidynilyas/actions-terraform.git'
            }

            withCredentials([usernamePassword(credentialsId: 'aws-creds', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
                stage ("Terraform Init") {
                    sh "terraform init"
                } 

                stage ("Terraform Plan") {
                    sh "terraform plan"
                }

                if (params.action == "apply"){
                    stage ("Terraform Apply") {
                        sh "terraform apply -auto-approve"
                    }
                }else{
                    stage ("Terraform Destroy") {
                        sh "terraform destroy -auto-approve"
                    }
                }
            }
        }
    }
}
