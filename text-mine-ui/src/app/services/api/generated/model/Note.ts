/**
 * text-mine-api
 * text-mine
 *
 * OpenAPI spec version: v1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

import * as models from './models';

export interface Note {
    documentId: string;

    type: string;

    subType: string;

    quote: string;

    lineRefs: Array<number>;

    section: number;

    content: string;

    score?: number;

    iri: string;

    id?: models.ObjectId;

    version?: number;

    createdDate?: Date;

    lastModifiedDate?: Date;

    createdBy?: string;

    lastModifiedBy?: string;

    hash?: string;

}
