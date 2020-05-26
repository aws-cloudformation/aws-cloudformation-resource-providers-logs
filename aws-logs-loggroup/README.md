# AWS::Logs::LogGroup

## Developing

- The JSON schema describing this resource is `aws-logs-loggroup.json`
- The RPDK will automatically generate the correct resource model from the
   schema whenever the project is built via Maven. You can also do this manually
   with the following command: `cfn-cli generate`
- Resource handlers live in `src/main/java/software/amazon/logs/loggroup`


Please don't modify files under `target/generated-sources/rpdk`, as they will be
automatically overwritten.

The code use [Lombok](https://projectlombok.org/), and [you may have to install
IDE integrations](https://projectlombok.org/) to enable auto-complete for
Lombok-annotated classes.

## Running Contract Tests

You can execute the following commands to run the tests.
You will need to have docker installed and running.

```bash
# Create a CloudFormation stack with development dependencies (a KMS CMK)
# NOTE: this has a monthly cost of 1 USD for the CMK
aws cloudformation deploy \
  --stack-name aws-logs-loggroup-dev-resources \
  --template-file dev-resources.yaml
# Write the stack output to overrides.json
aws cloudformation describe-stacks \
  --stack-name aws-logs-loggroup-dev-resources \
  --query "Stacks[0].Outputs[?OutputKey=='OverridesJson'].OutputValue" \
  --output text > overrides.json
# Package the code with Maven
mvn package
# Start the code as a lambda function in the background
# You can also run it in a separate terminal (without the & to run it in the foreground)
sam local start-lambda &
# Test the resource handlers by running them with credentials in your account
cfn test
# Stop the lambda function in the background
kill $(jobs -lp | tail -n1)
# Destroy the CLoudFormation stack
# NOTE: destroying and recreating will increase the monthly cost
aws cloudformation delete-stack \
  --stack-name aws-logs-loggroup-dev-resources
# Wait for the stack to be completly deleted
aws cloudformation wait stack-delete-complete \
  --stack-name aws-logs-loggroup-dev-resources
```

Currently the following tests are broken, see issue [\#25](https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/issues/25)

- contract_create_read_success
- contract_create_list_success
- contract_update_read_success
- contract_update_list_success
