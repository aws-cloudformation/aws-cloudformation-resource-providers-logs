# AWS::Logs::MetricFilter

## Development
The RPDK will automatically generate the correct resource model from the schema whenever the project is built via Maven. You can also do this manually with the following command: `cfn generate`.

> Please don't modify files under `target/generated-sources/rpdk`, as they will be automatically overwritten.

The code uses [Lombok](https://projectlombok.org/), and [you may have to install IDE integrations](https://projectlombok.org/setup/overview) to enable auto-complete for Lombok-annotated classes.

### Running contract tests

Prior to running `cfn test`, ensure that you have created a LogGroup with the name specified in file `overrides.json`. Do this in the AWS region that the tests run in.
