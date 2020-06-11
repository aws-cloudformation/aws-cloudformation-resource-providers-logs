# AWS::Logs::LogGroup

Resource schema for AWS::Logs::LogGroup

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Logs::LogGroup",
    "Properties" : {
        "<a href="#loggroupname" title="LogGroupName">LogGroupName</a>" : <i>String</i>,
        "<a href="#retentionindays" title="RetentionInDays">RetentionInDays</a>" : <i>Double</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Logs::LogGroup
Properties:
    <a href="#loggroupname" title="LogGroupName">LogGroupName</a>: <i>String</i>
    <a href="#retentionindays" title="RetentionInDays">RetentionInDays</a>: <i>Double</i>
</pre>

## Properties

#### LogGroupName

The name of the log group. If you don't specify a name, AWS CloudFormation generates a unique ID for the log group.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>512</code>

_Pattern_: <code>^[.\-_/#A-Za-z0-9]{1,512}\Z</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### RetentionInDays

The number of days to retain the log events in the specified log group. Possible values are: 1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545, 731, 1827, and 3653.

_Required_: No

_Type_: Double

_Allowed Values_: <code>1</code> | <code>3</code> | <code>5</code> | <code>7</code> | <code>14</code> | <code>30</code> | <code>60</code> | <code>90</code> | <code>120</code> | <code>150</code> | <code>180</code> | <code>365</code> | <code>400</code> | <code>545</code> | <code>731</code> | <code>1827</code> | <code>3653</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the LogGroupName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

The CloudWatch log group ARN.
