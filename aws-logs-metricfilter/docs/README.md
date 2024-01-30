# AWS::Logs::MetricFilter

Specifies a metric filter that describes how CloudWatch Logs extracts information from logs and transforms it into Amazon CloudWatch metrics.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Logs::MetricFilter",
    "Properties" : {
        "<a href="#filtername" title="FilterName">FilterName</a>" : <i>String</i>,
        "<a href="#filterpattern" title="FilterPattern">FilterPattern</a>" : <i>String</i>,
        "<a href="#loggroupname" title="LogGroupName">LogGroupName</a>" : <i>String</i>,
        "<a href="#metrictransformations" title="MetricTransformations">MetricTransformations</a>" : <i>[ <a href="metrictransformation.md">MetricTransformation</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Logs::MetricFilter
Properties:
    <a href="#filtername" title="FilterName">FilterName</a>: <i>String</i>
    <a href="#filterpattern" title="FilterPattern">FilterPattern</a>: <i>String</i>
    <a href="#loggroupname" title="LogGroupName">LogGroupName</a>: <i>String</i>
    <a href="#metrictransformations" title="MetricTransformations">MetricTransformations</a>: <i>
      - <a href="metrictransformation.md">MetricTransformation</a></i>
</pre>

## Properties

#### FilterName

A name for the metric filter.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>512</code>

_Pattern_: <code>^[^:*]{1,512}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### FilterPattern

Pattern that Logs follows to interpret each entry in a log.

_Required_: Yes

_Type_: String

_Maximum Length_: <code>1024</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogGroupName

Existing log group that you want to associate with this filter.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>512</code>

_Pattern_: <code>^[.\-_/#A-Za-z0-9]{1,512}</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### MetricTransformations

A collection of information that defines how metric data gets emitted.

_Required_: Yes

_Type_: List of <a href="metrictransformation.md">MetricTransformation</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

