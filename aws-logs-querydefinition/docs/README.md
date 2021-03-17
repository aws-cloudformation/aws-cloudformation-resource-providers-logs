# AWS::Logs::QueryDefinition

The resource schema for AWSLogs QueryDefinition

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Logs::QueryDefinition",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#querystring" title="QueryString">QueryString</a>" : <i>String</i>,
        "<a href="#loggroupnames" title="LogGroupNames">LogGroupNames</a>" : <i>[ String, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Logs::QueryDefinition
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#querystring" title="QueryString">QueryString</a>: <i>String</i>
    <a href="#loggroupnames" title="LogGroupNames">LogGroupNames</a>: <i>
      - String</i>
</pre>

## Properties

#### Name

A name for the saved query definition

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Pattern_: <code>^([^:*\/]+\/?)*[^:*\/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### QueryString

The query string to use for this definition

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>10000</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogGroupNames

Optionally define specific log groups as part of your query definition

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the QueryDefinitionId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### QueryDefinitionId

Unique identifier of a query definition
