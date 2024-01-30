# AWS::Logs::Destination

The AWS::Logs::Destination resource specifies a CloudWatch Logs destination. A destination encapsulates a physical resource (such as an Amazon Kinesis data stream) and enables you to subscribe that resource to a stream of log events.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Logs::Destination",
    "Properties" : {
        "<a href="#destinationname" title="DestinationName">DestinationName</a>" : <i>String</i>,
        "<a href="#destinationpolicy" title="DestinationPolicy">DestinationPolicy</a>" : <i>String</i>,
        "<a href="#rolearn" title="RoleArn">RoleArn</a>" : <i>String</i>,
        "<a href="#targetarn" title="TargetArn">TargetArn</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Logs::Destination
Properties:
    <a href="#destinationname" title="DestinationName">DestinationName</a>: <i>String</i>
    <a href="#destinationpolicy" title="DestinationPolicy">DestinationPolicy</a>: <i>String</i>
    <a href="#rolearn" title="RoleArn">RoleArn</a>: <i>String</i>
    <a href="#targetarn" title="TargetArn">TargetArn</a>: <i>String</i>
</pre>

## Properties

#### DestinationName

The name of the destination resource

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>512</code>

_Pattern_: <code>^[^:*]{1,512}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### DestinationPolicy

An IAM policy document that governs which AWS accounts can create subscription filters against this destination.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RoleArn

The ARN of an IAM role that permits CloudWatch Logs to send data to the specified AWS resource

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TargetArn

The ARN of the physical target where the log events are delivered (for example, a Kinesis stream)

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the DestinationName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.

