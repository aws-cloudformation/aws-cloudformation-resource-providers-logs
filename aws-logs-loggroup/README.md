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

1. Create a KMS CMK for use with CloudWatch Logs (see [the CloudWatch
    Documentation](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/encrypt-log-data-kms.html)).
2. Edit `overrides.json` so that every `/KmsKeyId` uses the ARN of that key.
3. Package the code with Maven (`mvn package`)
4. Run `sam local start-lambda` in one terminal
5. Run `cfn test` in another terminal. CFN test will use your credentials to test
    the resource handlers in your account.

Currently the following tests are broken, see issue [\#25](https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/issues/25)

- contract_create_read_success
- contract_create_list_success
- contract_update_read_success
- contract_update_list_success
