# AWS::Logs::MetricFilter MetricTransformations

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#defaultvalue" title="DefaultValue">DefaultValue</a>" : <i>Double</i>,
    "<a href="#metricname" title="MetricName">MetricName</a>" : <i>String</i>,
    "<a href="#metricnamespace" title="MetricNamespace">MetricNamespace</a>" : <i>String</i>,
    "<a href="#metricvalue" title="MetricValue">MetricValue</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#defaultvalue" title="DefaultValue">DefaultValue</a>: <i>Double</i>
<a href="#metricname" title="MetricName">MetricName</a>: <i>String</i>
<a href="#metricnamespace" title="MetricNamespace">MetricNamespace</a>: <i>String</i>
<a href="#metricvalue" title="MetricValue">MetricValue</a>: <i>String</i>
</pre>

## Properties

#### DefaultValue

The value to emit when a filter pattern does not match a log event. This value can be null.

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MetricName

The name of the CloudWatch metric. Metric name must be in ASCII format.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Pattern_: <code>^((?![:*$])[\x00-\x7F]){1,255}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MetricNamespace

The namespace of the CloudWatch metric.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>256</code>

_Pattern_: <code>^[0-9a-zA-Z\.\-_\/#]{1,256}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MetricValue

The value to publish to the CloudWatch metric when a filter pattern matches a log event.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Pattern_: <code>.{1,100}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
