/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.index;

import org.ballerinalang.langserver.common.utils.index.DAOUtil;
import org.ballerinalang.langserver.common.utils.index.DTOUtil;
import org.ballerinalang.langserver.index.dao.ObjectDAO;
import org.ballerinalang.langserver.index.dao.OtherTypeDAO;
import org.ballerinalang.langserver.index.dao.PackageFunctionDAO;
import org.ballerinalang.langserver.index.dao.RecordDAO;
import org.ballerinalang.langserver.index.dto.BFunctionDTO;
import org.ballerinalang.langserver.index.dto.BLangResourceDTO;
import org.ballerinalang.langserver.index.dto.BLangServiceDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.OtherTypeSymbolDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Query Processors for Language Server Index DB.
 */
public class LSIndexQueryProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LSIndexQueryProcessor.class);
 
    private PreparedStatement insertBLangPackage;
 
    private PreparedStatement insertBLangService;
 
    private PreparedStatement insertBLangResource;
 
    private PreparedStatement insertBLangFunction;
 
    private PreparedStatement insertBLangRecord;
 
    private PreparedStatement insertOtherType;
 
    private PreparedStatement insertBLangObject;
 
    private PreparedStatement updateActionHolderId;

    private PreparedStatement getFunctionsFromPackage;
    
    private PreparedStatement getRecordsFromPackage;
    
    private PreparedStatement getOtherTypesFromPackage;
    
    private PreparedStatement getObjectsFromPackage;

    LSIndexQueryProcessor(Connection connection) {
        // Generate the prepared statements
        try {
            // Insert Statements
            insertBLangPackage = connection.prepareStatement(Constants.INSERT_BLANG_PACKAGE, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangService = connection.prepareStatement(Constants.INSERT_BLANG_SERVICE, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangResource = connection.prepareStatement(Constants.INSERT_BLANG_RESOURCE, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangFunction = connection.prepareStatement(Constants.INSERT_BLANG_FUNCTION, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangRecord = connection.prepareStatement(Constants.INSERT_BLANG_RECORD, 
                    Statement.RETURN_GENERATED_KEYS);
            insertOtherType = connection.prepareStatement(Constants.INSERT_OTHER_TYPE, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangObject = connection.prepareStatement(Constants.INSERT_BLANG_OBJECT, 
                    Statement.RETURN_GENERATED_KEYS);

            // Update Statements
            updateActionHolderId = connection.prepareStatement(Constants.UPDATE_ENDPOINT_ACTION_HOLDER_ID, 
                    Statement.RETURN_GENERATED_KEYS);

            // Select Statements
            getFunctionsFromPackage = connection.prepareStatement(Constants.GET_FUNCTIONS_FROM_PACKAGE);
            getRecordsFromPackage = connection.prepareStatement(Constants.GET_RECORDS_FROM_PACKAGE);
            getOtherTypesFromPackage = connection.prepareStatement(Constants.GET_OTHER_TYPES_FROM_PACKAGE);
            getObjectsFromPackage = connection.prepareStatement(Constants.GET_OBJECT_FROM_PACKAGE);
        } catch (SQLException e) {
            logger.error("Error in Creating Prepared Statement.");
        }
    }

    /**
     * Batch Insert List of PackageSymbolDTOs.
     * @param packageDTOs       List of Package DTOs
     * @return {@link List}     List of Generated Keys
     * @throws SQLException     Exception While Insert
     */
    public List<Integer> batchInsertBPackageSymbols(List<BPackageSymbolDTO> packageDTOs) throws SQLException {
        clearBatch(insertBLangPackage);
        for (BPackageSymbolDTO packageDTO : packageDTOs) {
            insertBLangPackage.setString(1, packageDTO.getPackageID().getName());
            insertBLangPackage.setString(2, packageDTO.getPackageID().getOrgName());
            insertBLangPackage.setString(3, packageDTO.getPackageID().getVersion());
            insertBLangPackage.addBatch();
        }
        insertBLangPackage.executeBatch();

        return this.getGeneratedKeys(insertBLangPackage.getGeneratedKeys());
    }

    /**
     * Batch Insert List of Service Symbols.
     * @param bLangServiceDTOs  List of ServiceSymbolDTOs
     * @return {@link List}     List of Generated Keys
     * @throws SQLException     Exception While Insert
     */
    public List<Integer> batchInsertServiceSymbols(List<BLangServiceDTO> bLangServiceDTOs) throws SQLException {
        clearBatch(insertBLangService);
        for (BLangServiceDTO bLangServiceDTO : bLangServiceDTOs) {
            insertBLangService.setInt(1, bLangServiceDTO.getPackageId());
            insertBLangService.setString(2, bLangServiceDTO.getName());
            insertBLangService.addBatch();
        }
        insertBLangService.executeBatch();

        return this.getGeneratedKeys(insertBLangService.getGeneratedKeys());
    }

    /**
     * Batch Insert List of Resource Symbols.
     * @param bLangResourceDTOs  List of BLangResourceDTOs
     * @return {@link List}     List of Generated Keys
     * @throws SQLException     Exception While Insert
     */
    public List<Integer> batchInsertBLangResources(List<BLangResourceDTO> bLangResourceDTOs) throws SQLException {
        clearBatch(insertBLangResource);
        for (BLangResourceDTO bLangResourceDTO : bLangResourceDTOs) {
            insertBLangResource.setInt(1, bLangResourceDTO.getServiceId());
            insertBLangResource.setString(2, bLangResourceDTO.getName());
            insertBLangResource.addBatch();
        }
        insertBLangResource.executeBatch();

        return this.getGeneratedKeys(insertBLangResource.getGeneratedKeys());
    }

    /**
     * Batch Insert List of BInvokable Symbols.
     * @param bFunctionDTOs     List of BFunctionDTOs
     * @return {@link List}     List of Generated Keys
     * @throws SQLException     Exception While Insert
     */
    public List<Integer> batchInsertBLangFunctions(List<BFunctionDTO> bFunctionDTOs)
         throws SQLException, IOException {
        clearBatch(insertBLangFunction);

        for (BFunctionDTO bFunctionDTO : bFunctionDTOs) {
            insertBLangFunction.setInt(1, bFunctionDTO.getPackageId());
            insertBLangFunction.setInt(2, bFunctionDTO.getObjectId());
            insertBLangFunction.setString(3, bFunctionDTO.getName());
            insertBLangFunction.setString(4, DTOUtil.completionItemToJSON(bFunctionDTO.getCompletionItem()));
            insertBLangFunction.addBatch();
        }
        insertBLangFunction.executeBatch();

        return this.getGeneratedKeys(insertBLangFunction.getGeneratedKeys());
    }

    /**
     * Batch Insert List of RecordTypeSymbols.
     * @param recordDTOs        List of BRecordTypeSymbolDTOs
     * @return {@link List}     List of Generated Keys
     * @throws SQLException     Exception While Insert
     */
    public List<Integer> batchInsertBLangRecords(List<BRecordTypeSymbolDTO> recordDTOs) throws SQLException,
            IOException {
        clearBatch(insertBLangRecord);
        for (BRecordTypeSymbolDTO recordDTO : recordDTOs) {
            insertBLangRecord.setInt(1, recordDTO.getPackageId());
            insertBLangRecord.setString(2, recordDTO.getName());
            // TODO: Currently null
            insertBLangRecord.setString(3, "");
            insertBLangRecord.setString(4, DTOUtil.completionItemToJSON(recordDTO.getCompletionItem()));
            insertBLangRecord.addBatch();
        }

        insertBLangRecord.executeBatch();

        return this.getGeneratedKeys(insertBLangRecord.getGeneratedKeys());
    }

    /**
     * Batch Insert List of Other Type Symbols.
     * @param otherTypeSymbolDTOs   list of BRecordTypeSymbolDTOs
     * @return {@link List}         List of Generated Keys
     * @throws SQLException         Exception While Insert
     */
    public List<Integer> batchInsertOtherTypes(List<OtherTypeSymbolDTO> otherTypeSymbolDTOs) throws SQLException,
            IOException {
        clearBatch(insertOtherType);
        for (OtherTypeSymbolDTO otherTypeDTO : otherTypeSymbolDTOs) {
            insertOtherType.setInt(1, otherTypeDTO.getPackageId());
            insertOtherType.setString(2, otherTypeDTO.getName());
            // TODO: Currently null
            insertOtherType.setString(3, "");
            insertOtherType.setString(4, DTOUtil.completionItemToJSON(otherTypeDTO.getCompletionItem()));
            insertOtherType.addBatch();
        }

        insertOtherType.executeBatch();

        return this.getGeneratedKeys(insertOtherType.getGeneratedKeys());
    }

    /**
     * Batch Insert List of ObjectTypeSymbols.
     * @param objectDTOs        List of BFunctionDTOs
     * @return {@link List}     List of Generated Keys
     * @throws SQLException     Exception While Insert
     */
    public List<Integer> batchInsertBLangObjects(List<BObjectTypeSymbolDTO> objectDTOs) throws SQLException,
            IOException {
        clearBatch(insertBLangObject);
        for (BObjectTypeSymbolDTO objectDTO : objectDTOs) {
            insertBLangObject.setInt(1, objectDTO.getPackageId());
            insertBLangObject.setString(2, objectDTO.getName());
            // TODO: still null. handle properly
            insertBLangObject.setString(3, "");
            insertBLangObject.setInt(4, objectDTO.getType().getValue());
            insertBLangObject.setString(5, DTOUtil.completionItemToJSON(objectDTO.getCompletionItem()));
            insertBLangObject.addBatch();
        }

        insertBLangObject.executeBatch();
        return this.getGeneratedKeys(insertBLangObject.getGeneratedKeys());
    }

    /**
     * Update Action holderId entry of endpoint nodes
     *
     * Note: endpoint IDs order and te actionHolder IDs holder order are equal.
     * @param endpoints         list of Endpoint IDs
     * @param actionHolders     list of Action holder IDs
     * @return {@link List}     List of Generated Keys
     */
    public List<Integer> batchUpdateActionHolderId(List<Integer> endpoints, List<Integer> actionHolders)
            throws SQLException {
        clearBatch(updateActionHolderId);
        for (int i = 0; i < endpoints.size(); i++) {
            updateActionHolderId.setInt(1, actionHolders.get(i));
            updateActionHolderId.setInt(2, endpoints.get(i));
            updateActionHolderId.addBatch();
        }
        
        updateActionHolderId.executeBatch();
        
        return this.getGeneratedKeys(updateActionHolderId.getGeneratedKeys());
    }

    // Get Statements
    /**
     * Get a List of PackageFunctionDAOs.
     * @param name                          Package Name
     * @param orgName                       Org Name
     * @return {@link PackageFunctionDAO}   List of FunctionDAOs
     * @throws SQLException                 Exception While Insert
     */
    public List<PackageFunctionDAO> getFunctionsFromPackage(String name, String orgName)
            throws SQLException {
        getFunctionsFromPackage.clearParameters();
        getFunctionsFromPackage.setString(1, name);
        getFunctionsFromPackage.setString(2, orgName);
        
        return DAOUtil.getPackageFunctionDAO(getFunctionsFromPackage.executeQuery());
    }

    /**
     * Get a List of RecordDAOs.
     * @param name                  Package Name
     * @param orgName               Org Name
     * @return {@link RecordDAO}    List of RecordDAOs
     * @throws SQLException         Exception While Insert
     */
    public List<RecordDAO> getRecordsFromPackage(String name, String orgName) throws SQLException {
        getRecordsFromPackage.clearParameters();
        getRecordsFromPackage.setString(1, name);
        getRecordsFromPackage.setString(2, orgName);
        
        return DAOUtil.getRecordDAO(getRecordsFromPackage.executeQuery());
    }

    /**
     * Get a List of RecordDAOs.
     * @param name                      Package Name
     * @param orgName                   Org Name
     * @return {@link OtherTypeDAO}     List of OtherTypeDAOs
     * @throws SQLException             Exception While Insert
     */
    public List<OtherTypeDAO> getOtherTypesFromPackage(String name, String orgName) throws SQLException {
        getOtherTypesFromPackage.clearParameters();
        getOtherTypesFromPackage.setString(1, name);
        getOtherTypesFromPackage.setString(2, orgName);

        return DAOUtil.getOtherTypeDAO(getOtherTypesFromPackage.executeQuery());
    }

    /**
     * Get a List of ObjectDAOs.
     * @param name                  Package Name
     * @param orgName               Org Name
     * @return {@link ObjectDAO}    List of FunctionDAOs
     * @throws SQLException         Exception While Insert
     */
    public List<ObjectDAO> getObjectsFromPackage(String name, String orgName) throws SQLException {
        getObjectsFromPackage.clearParameters();
        getObjectsFromPackage.setString(1, name);
        getObjectsFromPackage.setString(2, orgName);
        
        return DAOUtil.getObjectDAO(getObjectsFromPackage.executeQuery());
    }

    // Private Methods

    private static void clearBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.clearBatch();
    }

    private List<Integer> getGeneratedKeys(ResultSet resultSet) {
        List<Integer> generatedKeys = new ArrayList<>();
        try {
            while (resultSet.next()) {
                generatedKeys.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error getting the Generated Keys: [" + e.getMessage() + "]");
        }
        return generatedKeys;
    }
}
