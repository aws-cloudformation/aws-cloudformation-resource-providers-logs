# AWS::Logs::LogStream

Resource Type definition for AWS::Logs::LogStream

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Logs::LogStream",
    "Properties" : {
        "<a href="#loggroupname" title="LogGroupName">LogGroupName</a>" : <i>String</i>,
        "<a href="#logstreamname" title="LogStreamName">LogStreamName</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Logs::LogStream
Properties:
    <a href="#loggroupname" title="LogGroupName">LogGroupName</a>: <i>String</i>
    <a href="#logstreamname" title="LogStreamName">LogStreamName</a>: <i>String</i>
</pre>

## Properties

#### LogGroupName

The name of the log group where the log stream is created.

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### LogStreamName

The name of the log stream. The name must be unique wihtin the log group.

_Required_: No

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

