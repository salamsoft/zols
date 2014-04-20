package org.zols.datastore.web.controller;

import com.mangofactory.swagger.annotations.ApiIgnore;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.zols.datastore.DataStore;
import org.zols.datastore.domain.BaseObject;
import org.zols.datastore.domain.Entity;
import org.zols.datastore.exception.DataStoreException;
import org.zols.datastore.util.DynamicBeanGenerator;

@Controller
@Api(value = "Entities")
public class EntityController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EntityController.class);

    @Autowired
    private DataStore dataStore;

    @Autowired
    private DynamicBeanGenerator dynamicBeanGenerator;

    @ApiOperation(value = "Creates an Entity", response = Entity.class, notes = "Returns the created entity")
    @RequestMapping(value = "/api/entities", method = POST,consumes = APPLICATION_JSON_VALUE , produces = APPLICATION_JSON_VALUE)    
    @ResponseBody
    public Entity create(@RequestBody Entity entity) {
        LOGGER.info("Creating new entity {}", entity);
        return dataStore.create(entity, Entity.class);
    }

    @ApiOperation(value = "Read", response =Map.class ,notes = "Return entity")
    @RequestMapping(value = "/api/entities/{name}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Entity> read(@PathVariable(value = "name") String name) {
        LOGGER.info("Reading entity with id {}", name);
        Map<String, Entity> map = new HashMap<String, Entity>(1);
        map.put("entity", dataStore.read(name, Entity.class));
        return map;
    }

    @ApiOperation(value = "Update", notes = "Updates an entity")
    @RequestMapping(value = "/api/entities/{name}", method = PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void update(@PathVariable(value = "name") String name,
            @RequestBody Entity entity) {
        LOGGER.info("Updating entity with id {} with {}", name, entity);
        if (name.equals(entity.getName())) {
            dataStore.update(entity, Entity.class);
        } else {
            throw new DataStoreException("Invalid Record");
        }
    }

    @ApiOperation(value = "Delete", notes = "Deletes an entity")
    @RequestMapping(value = "/api/entities/{name}", method = DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "name") String name) {
        LOGGER.info("Deleting entity with id {}", name);
        dataStore.delete(name, Entity.class);
    }

    @ApiOperation(value = "List", response = Page.class,notes = "List all entity")
    @RequestMapping(value = "/api/entities", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Page<Entity> list(
            Pageable page) {
        LOGGER.info("Listing entities");
        return dataStore.list(page, Entity.class);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleClientErrors(Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
        return ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleServerErrors(Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
        return ex.getMessage();
    }

    @RequestMapping(value = "/entities/{name}", method = GET)
    @ApiIgnore
    public String edit(@PathVariable(value = "name") String name, Model model) {
        model.addAttribute("entity", dataStore.read(name, Entity.class));
        return "org/zols/datastore/entity";
    }

    @RequestMapping(value = "/entities/add", method = GET)
    @ApiIgnore
    public String add(Model model) {
        model.addAttribute("entity", new Entity());
        return "org/zols/datastore/entity";
    }

    @RequestMapping(value = "/entities", method = GET)
    @ApiIgnore
    public String listing() {
        return "org/zols/datastore/listentities";
    }

    //dataList mapping
    @RequestMapping(value = "/data/{entityName}", method = GET)
    @ApiIgnore
    public String listdata(@PathVariable(value = "entityName") String entityName, Model model) {
        model.addAttribute("entity", dataStore.read(entityName, Entity.class));
        return "org/zols/datastore/listdata";
    }

    @RequestMapping(value = "/data/{entityName}/add", method = GET)
    @ApiIgnore
    public String addData(@PathVariable(value = "entityName") String entityName, Model model) {
        model.addAttribute("entityName", entityName);
        return "org/zols/datastore/data";
    }

    @RequestMapping(value = "/data/{entityName}/{dataName:.+}", method = GET)
    @ApiIgnore
    public String editData(@PathVariable(value = "entityName") String entityName,
            @PathVariable(value = "dataName") String dataName,
            Model model) {
        model.addAttribute("entityName", entityName);
        model.addAttribute("dataName", dataName);
        return "org/zols/datastore/data";
    }

    @ApiOperation(value = "Read data", response = BaseObject.class,notes = "Read entity")
    @RequestMapping(value = "api/data/{entityName}/{dataName:.+}", method = GET)
    @ResponseBody
    public BaseObject readData(@PathVariable(value = "entityName") String entityName,
            @PathVariable(value = "dataName") String dataName,
            Model model) {
        Class<? extends BaseObject> clazz = dynamicBeanGenerator.getBeanClass(entityName);
        return dataStore.read(dataName, clazz);
    }

    @ApiOperation(value = "create", response = BaseObject.class,notes = "create an entity")
    @RequestMapping(value = "/api/data/{entityName}", method = POST)
    @ResponseBody
    public BaseObject create(@PathVariable(value = "entityName") String entityName, @RequestBody HashMap<String, String> entityObjectMap) {
        Class<? extends BaseObject> clazz = dynamicBeanGenerator.getBeanClass(entityName);
        return dataStore.create(dataStore.getBaseObject(clazz, entityName, entityObjectMap), clazz);
    }

    @ApiOperation(value = "Update", notes = "update datastore entity")
    @RequestMapping(value = "/api/data/{entityName}/{name:.+}", method = PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void update(@PathVariable(value = "entityName") String entityName, @PathVariable(value = "name") String name,
            @RequestBody HashMap<String, String> entityObjectMap) {
        Class<? extends BaseObject> clazz = dynamicBeanGenerator.getBeanClass(entityName);
        BaseObject baseObject = dataStore.getBaseObject(clazz, entityName, entityObjectMap);
        if (name.equals(baseObject.getName())) {
            if (dataStore.read(baseObject.getName(), clazz) != null) {
                dataStore.update(baseObject, clazz);
            } else {
                throw new DataStoreException("Record does not exist");
            }
        } else {
            throw new DataStoreException("Invalid Record");
        }
    }

    @ApiOperation(value = "Delete", notes = "delete datastore entity")
    @RequestMapping(value = "/api/data/{entityName}/{name:.+}", method = DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "entityName") String entityName, @PathVariable(value = "name") String name) {
        Class<? extends BaseObject> clazz = dynamicBeanGenerator.getBeanClass(entityName);
        dataStore.delete(name, clazz);
    }

     @ApiOperation(value = "List",response = Page.class, notes = "List datastore")
    @RequestMapping(value = "/api/data/{entityName}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Page<BaseObject> list(@PathVariable(value = "entityName") String entityName,
            Pageable page) {
        return dataStore.list(entityName, page);
    }   
}
