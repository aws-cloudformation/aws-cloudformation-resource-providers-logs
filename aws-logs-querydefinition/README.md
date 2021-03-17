# AWS::Logs::QueryDefinition

This repository is for the QueryDefinition CloudFormation resource type.

*To get started run:*
```
mvn generate-sources && mvn package
```

*To run an integration test against an AWS account with a custom json template run the following command:*

```
sam local invoke TestEntrypoint --event sam-tests/some-crudl-call.json
```

See more [here](https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-walkthrough.html) for what to put in the json files

*To run Contract Tests, run*
```
cfn test
```
or
```
cfn test -- -k contract_some_test
```
The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: `cfn generate`.

> Please don't modify files under `target/generated-sources/rpdk`, as they will be automatically overwritten.

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.
