# AWS::Logs::MetricFilter MetricTransformation

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#defaultvalue" title="DefaultValue">DefaultValue</a>" : <i>Double</i>,
    "<a href="#metricname" title="MetricName">MetricName</a>" : <i>String</i>,
    "<a href="#metricnamespace" title="MetricNamespace">MetricNamespace</a>" : <i>String</i>,
    "<a href="#metricvalue" title="MetricValue">MetricValue</a>" : <i>String</i>,
    "<a href="#unit" title="Unit">Unit</a>" : <i>String</i>,
    "<a href="#dimensions" title="Dimensions">Dimensions</a>" : <i>[ <a href="dimension.md">Dimension</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#defaultvalue" title="DefaultValue">DefaultValue</a>: <i>Double</i>
<a href="#metricname" title="MetricName">MetricName</a>: <i>String</i>
<a href="#metricnamespace" title="MetricNamespace">MetricNamespace</a>: <i>String</i>
<a href="#metricvalue" title="MetricValue">MetricValue</a>: <i>String</i>
<a href="#unit" title="Unit">Unit</a>: <i>String</i>
<a href="#dimensions" title="Dimensions">Dimensions</a>: <i>
      - <a href="dimension.md">Dimension</a></i>
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

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>255</code>

_Pattern_: <code>^((?![:*$])[\x00-\x7F]){1,255}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MetricNamespace

The namespace of the CloudWatch metric.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>256</code>

_Pattern_: <code>^[0-9a-zA-Z\.\-_\/#]{1,256}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MetricValue

The value to publish to the CloudWatch metric when a filter pattern matches a log event.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>100</code>

_Pattern_: <code>.{1,100}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Unit

The unit to assign to the metric. If you omit this, the unit is set as None.

_Required_: No

_Type_: String

_Allowed Values_: <code>Seconds</code> | <code>Microseconds</code> | <code>Milliseconds</code> | <code>Bytes</code> | <code>Kilobytes</code> | <code>Megabytes</code> | <code>Gigabytes</code> | <code>Terabytes</code> | <code>Bits</code> | <code>Kilobits</code> | <code>Megabits</code> | <code>Gigabits</code> | <code>Terabits</code> | <code>Percent</code> | <code>Count</code> | <code>Bytes/Second</code> | <code>Kilobytes/Second</code> | <code>Megabytes/Second</code> | <code>Gigabytes/Second</code> | <code>Terabytes/Second</code> | <code>Bits/Second</code> | <code>Kilobits/Second</code> | <code>Megabits/Second</code> | <code>Gigabits/Second</code> | <code>Terabits/Second</code> | <code>Count/Second</code> | <code>None</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Dimensions

Dimensions are the key-value pairs that further define a metric

_Required_: No

_Type_: List of <a href="dimension.md">Dimension</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

