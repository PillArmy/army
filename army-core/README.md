## army-core

This module is core module,provide below:

* table and column meta
* criteria api
* standard criteria api
* standard statement parser
* environment
* Mapping mechanism

## MappingType Factory Method Declaration Rules

Factory methods in `MappingType` implementations must follow these rules:

### Basic Requirements

| Rule        | Description                  |
|-------------|------------------------------|
| Modifier    | public static                |
| Return type | Same class as declaring type |
| Method name | Start with "from"            |
| Parameters  | At least 1 parameter         |

### Method Signature Rules

| Method Name              | Parameter Count | Parameter Types                   |
|--------------------------|-----------------|-----------------------------------|
| from                     | 1               | Class                             |
| fromJavaField            | 1               | Field                             |
| fromList                 | 1               | Class                             |
| fromSet                  | 1               | Class                             |
| fromEnumSet              | 1               | Class                             |
| fromMap                  | 2               | Class, Class                      |
| fromEnumMap              | 2               | Class, Class                      |
| fromTypeArg              | 2               | Class, Class                      |
| fromTypeArgs             | 2 or more       | Class, Class... or Class, Class[] |
| fromTypeArgAndType       | 3               | Class, Class, MappingType         |
| fromTypeArgsAndTypes     | 3               | Class, Class[], MappingType[]     |
| fromTypeArgChain         | 3 or more       | Class, Class or Class[]...        |
| fromTypeArgChainAndTypes | Odd number (3+) | Class, Class[], MappingType[]...  |
| fromParam                | 2               | Class, String                     |
| fromParams               | 2 or more       | Class, String...                  |