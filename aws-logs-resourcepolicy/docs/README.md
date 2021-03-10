# AWS::Logs::ResourcePolicy

The resource schema for AWSLogs ResourcePolicy

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Logs::ResourcePolicy",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#policydocument" title="PolicyDocument">PolicyDocument</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Logs::ResourcePolicy
Properties:
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#policydocument" title="PolicyDocument">PolicyDocument</a>: <i>String</i>
</pre>

## Properties

#### Name

A name for resource policy

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>255</code>

_Pattern_: <code>^([^:*\/]+\/?)*[^:*\/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PolicyDocument

The policy document

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>131072</code>

_Pattern_: <code>[\u0009\u000A\u000D\u0020-\u00FF]+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Name.
