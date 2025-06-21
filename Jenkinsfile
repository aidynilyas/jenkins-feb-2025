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

properties([
    parameters
    ([
        choice(choices: ['apply', 'destroy'], description: 'actions', name: 'action'),
        choice(choices: ['us-east-1', 'us-east-2', 'us-west-1', 'us-west-2'], description: 'Select US region (East 1 & 2, West 1 & 2)', name: 'region'), 
        string(description: 'AMI ID for the EC2', name: 'ami_id', trim: true), 
        string(defaultValue: 't2.micro', description: 'Instance type', name: 'instance_type', trim: true)
    ])
])

tfvars = 
"""
region = "${params.region}"
ami_id = "${params.ami_id}"
instance_type = "${params.instance_type}"
"""

podTemplate(cloud: 'kubernetes', label: 'terraform', yaml: template) {
    node ("terraform") {
        container ("terraform"){
            stage ("Checkout SCM") {
                git branch: 'main', url: 'https://github.com/aidynilyas/actions-terraform.git'
            }

            withCredentials([usernamePassword(credentialsId: 'aws-creds', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
                stage ("Terraform Init") {
                    sh "terraform init -backend-config=key=${params.region}/terraform.tfstate"
                } 
                        
                writeFile file: 'kaizen.tfvars', text: tfvars

                stage ("Terraform Plan") {
                    sh "terraform plan  -var-file kaizen.tfvars"
                }

                if (params.action == "apply"){
                    stage ("Terraform Apply") {
                        sh "terraform apply -auto-approve -var-file kaizen.tfvars"
                    }
                }else{
                    stage ("Terraform Destroy") {
                        sh "terraform destroy -auto-approve -var-file kaizen.tfvars"
                    }
                }
                
            }
        }
    }
}
