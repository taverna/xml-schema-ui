xml-schema-ui
=============

Swing XML Schema UI library

This library is to facilitate visual XML creation/parsing.
The key class of the library is SchemaTreeModel which must be provided with known XML Schemas: model.setSchemaCollection(schemas)

After that, the model may be either set programatically: model.addElement(new QName("http://example.com","el_name")),
or automatically, via provided XML file: model.read(reader)

Some usage examples may be extracted from jUnit tests.
