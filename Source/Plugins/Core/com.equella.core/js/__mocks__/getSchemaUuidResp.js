/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
exports.getSchemaUuidResp = {
  uuid: "f028746b-0346-fe7e-a223-a2db1240140c",
  name: "CAL Guide Schema",
  nameStrings: {
    en: "CAL Guide Schema",
  },
  description:
    "testing the creation of a CAL schema as demonstrated by the CAL configuration guide",
  descriptionStrings: {
    en:
      "testing the creation of a CAL schema as demonstrated by the CAL configuration guide",
  },
  modifiedDate: "2014-04-04T12:56:52.870+11:00",
  createdDate: "2012-03-14T17:33:05.767+11:00",
  owner: {
    id: "adfcaf58-241b-4eca-9740-6a26d1c3dd58",
  },
  security: {
    rules: [],
  },
  namePath: "/item/itembody/name",
  descriptionPath: "/item/itembody/description",
  definition: {
    xml: {
      item: {
        itembody: {
          name: {
            _indexed: true,
            _field: true,
            _type: "text",
          },
          description: {
            _indexed: true,
            _type: "text",
          },
        },
        copyright: {
          "@parenttype": {
            _nested: true,
            _type: "text",
          },
          issue: {
            type: {
              _indexed: true,
              _type: "text",
            },
            value: {
              _indexed: true,
              _type: "text",
            },
          },
          "@type": {
            _nested: true,
            _type: "text",
          },
          isbn: {
            _indexed: true,
            _field: true,
            _type: "text",
          },
          abstract: {
            _indexed: true,
            _type: "text",
          },
          title: {
            _indexed: true,
            _field: true,
            _type: "text",
          },
          volume: {
            _indexed: true,
            _type: "text",
          },
          pages: {
            _type: "text",
          },
          issn: {
            _indexed: true,
            _field: true,
            _type: "text",
          },
          publication: {
            year: {
              _type: "text",
            },
            place: {
              _indexed: true,
              _field: true,
              _type: "text",
            },
          },
          publisher: {
            _type: "text",
          },
          editors: {
            editor: {
              _indexed: true,
              _field: true,
              _type: "text",
            },
          },
          authors: {
            author: {
              _indexed: true,
              _field: true,
              _type: "text",
            },
          },
          portions: {
            portion: {
              number: {
                _type: "text",
              },
              topics: {
                topic: {
                  _indexed: true,
                  _type: "text",
                },
              },
              abstract: {
                _indexed: true,
                _type: "text",
              },
              title: {
                _indexed: true,
                _field: true,
                _type: "text",
              },
              sections: {
                section: {
                  copyrightstatus: {
                    _type: "text",
                  },
                  pages: {
                    _type: "text",
                  },
                  attachment: {
                    _type: "text",
                  },
                  illustration: {
                    _type: "text",
                  },
                  type: {
                    _type: "text",
                  },
                },
              },
              authors: {
                author: {
                  _indexed: true,
                  _field: true,
                  _type: "text",
                },
              },
            },
          },
        },
      },
    },
  },
  citations: [
    {
      name: "Harvard",
      transformation: "harvard.xsl",
    },
  ],
  exportTransformsMap: {},
  importTransformsMap: {
    "Z39.50 to Book": "MODS_z3950_to_book-20071212-1.xslt",
  },
  ownerUuid: "adfcaf58-241b-4eca-9740-6a26d1c3dd58",
  serializedDefinition:
    '<xml><item><copyright><type attribute="true" type="text"/><parenttype attribute="true" type="text"/><title field="true" search="true" type="text"/><abstract search="true" type="text"/><isbn field="true" search="true" type="text"/><authors><author field="true" search="true" type="text"/></authors><editors><editor field="true" search="true" type="text"/></editors><issn field="true" search="true" type="text"/><issue><type search="true" type="text"/><value search="true" type="text"/></issue><volume search="true" type="text"/><publisher type="text"/><publication><year type="text"/><place field="true" search="true" type="text"/></publication><pages type="text"/><portions><portion><title field="true" search="true" type="text"/><authors><author field="true" search="true" type="text"/></authors><abstract search="true" type="text"/><number type="text"/><topics><topic search="true" type="text"/></topics><sections><section><pages type="text"/><attachment type="text"/><type type="text"/><copyrightstatus type="text"/><illustration type="text"/></section></sections></portion></portions></copyright><itembody><name field="true" search="true" type="text"/><description search="true" type="text"/></itembody></item></xml>',
  links: {
    self:
      "http://localhost:8080/rest/api/schema/f028746b-0346-fe7e-a223-a2db1240140c",
  },
};
