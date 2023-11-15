# AWS::Logs::LogAnomalyDetector

The AWS::Logs::LogAnomalyDetector resource specifies a CloudWatch Logs LogAnomalyDetector.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Logs::LogAnomalyDetector",
    "Properties" : {
        "<a href="#accountid" title="AccountId">AccountId</a>" : <i>String</i>,
        "<a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>" : <i>String</i>,
        "<a href="#detectorname" title="DetectorName">DetectorName</a>" : <i>String</i>,
        "<a href="#loggrouparnlist" title="LogGroupArnList">LogGroupArnList</a>" : <i>[ String, ... ]</i>,
        "<a href="#evaluationfrequency" title="EvaluationFrequency">EvaluationFrequency</a>" : <i>String</i>,
        "<a href="#filterpattern" title="FilterPattern">FilterPattern</a>" : <i>String</i>,
        "<a href="#anomalyvisibilitytime" title="AnomalyVisibilityTime">AnomalyVisibilityTime</a>" : <i>Double</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Logs::LogAnomalyDetector
Properties:
    <a href="#accountid" title="AccountId">AccountId</a>: <i>String</i>
    <a href="#kmskeyid" title="KmsKeyId">KmsKeyId</a>: <i>String</i>
    <a href="#detectorname" title="DetectorName">DetectorName</a>: <i>String</i>
    <a href="#loggrouparnlist" title="LogGroupArnList">LogGroupArnList</a>: <i>
      - String</i>
    <a href="#evaluationfrequency" title="EvaluationFrequency">EvaluationFrequency</a>: <i>String</i>
    <a href="#filterpattern" title="FilterPattern">FilterPattern</a>: <i>String</i>
    <a href="#anomalyvisibilitytime" title="AnomalyVisibilityTime">AnomalyVisibilityTime</a>: <i>Double</i>
</pre>

## Properties

#### AccountId

Account ID for owner of detector

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KmsKeyId

The Amazon Resource Name (ARN) of the CMK to use when encrypting log data.

_Required_: No

_Type_: String

_Maximum Length_: <code>256</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DetectorName

Name of detector

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogGroupArnList

List of Arns for the given log group

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EvaluationFrequency

How often log group is evaluated

_Required_: No

_Type_: String

_Allowed Values_: <code>FIVE_MIN</code> | <code>TEN_MIN</code> | <code>FIFTEEN_MIN</code> | <code>THIRTY_MIN</code> | <code>ONE_HOUR</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FilterPattern

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AnomalyVisibilityTime

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the AnomalyDetectorArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AnomalyDetectorArn

ARN of LogAnomalyDetector

#### CreationTimeStamp

When detector was created.

#### LastModifiedTimeStamp

When detector was lsat modified.

#### AnomalyDetectorStatus

Current status of detector.

