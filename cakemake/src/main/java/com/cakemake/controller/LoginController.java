package com.cakemake.controller;

import com.cakemake.dao.CakeRepository;
import com.cakemake.dao.CartRepository;
import com.cakemake.dao.OrdersRepository;
import com.cakemake.dao.UserRepository;
import com.cakemake.exception.ResourceNotFoundException;
import com.cakemake.model.CakeMaster;
import com.cakemake.model.Cart;
import com.cakemake.model.Orders;
import com.cakemake.model.User;
import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.Clob;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Controller
public class LoginController {

    @Autowired
    CakeRepository cakeRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrdersRepository ordersRepository;

    @Autowired
    HttpServletRequest httpServletRequest;



    @Autowired
    @PersistenceContext
    EntityManager em;

    @Autowired
    ServletContext servletContext;
    @Autowired
    HttpServletResponse httpServletResponse;

    @Autowired
    private Environment env;



    @GetMapping("/")
    public String index(){
        HttpSession session = httpServletRequest.getSession();
        session.removeAttribute("username");
        session.removeAttribute("emailId");
        session.invalidate();
        return "index";
    }

    @GetMapping("/add")
    public String add(){
        return "addCake";
    }


    @GetMapping("/deleteAll")
    @ResponseBody
    public Iterable<CakeMaster> delete(){
        cakeRepository.deleteAll();
        return cakeRepository.findAll();
    }

    @DeleteMapping("/deleteById")
    @ResponseBody
    public Iterable<CakeMaster> deleteById(@RequestParam(value="id") Long id){
        cakeRepository.deleteById(id);
        return cakeRepository.findAll();
    }

    @PostMapping("/addCake")
    @ResponseBody
    public CakeMaster addCake() throws Exception{
        CakeMaster cakeMaster = new CakeMaster();
        cakeMaster.setType(httpServletRequest.getParameter("type"));
        cakeMaster.setCategory(httpServletRequest.getParameter("category"));
        cakeMaster.setDescription(httpServletRequest.getParameter("description"));
        cakeMaster.setFeature(httpServletRequest.getParameter("feature"));
        cakeMaster.setFlavor(httpServletRequest.getParameter("flavor"));
        cakeMaster.setName(httpServletRequest.getParameter("name"));
        cakeMaster.setPrice(Long.parseLong(httpServletRequest.getParameter("price")));
        cakeMaster.setQuantity(Long.parseLong(httpServletRequest.getParameter("quantity")));
        cakeMaster.setShape(httpServletRequest.getParameter("shape"));
        cakeMaster.setWeight(httpServletRequest.getParameter("weight"));

        String picture = httpServletRequest.getParameter("picture");
       /* byte[] values = picture.getBytes();
        Blob blob = new SerialBlob(values);
        cakeMaster.setPicture(blob);*/

        LobCreator lobCreator = Hibernate.getLobCreator(em.unwrap(Session.class));
        Blob blob = lobCreator.createBlob(picture.getBytes());
        cakeMaster.setPicture(blob);



        System.out.println("img : > "+httpServletRequest.getParameter("picture"));
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = now.format(formatter);
        cakeMaster.setCreated_date(formatDateTime);

        CakeMaster cakeMaster1 = cakeRepository.save(cakeMaster);
        return cakeMaster1;
    }


    @GetMapping("/getAll")
    @ResponseBody
    public Iterable<CakeMaster> getAll() throws  Exception{

        Iterable<CakeMaster> cakeMasters= cakeRepository.findAll();
        Iterator iterator = cakeMasters.iterator();
        List<CakeMaster> cakeMasters1List = new ArrayList<>();

        while (iterator.hasNext()){
            CakeMaster cakeMaster = (CakeMaster) iterator.next();
            Blob blob = cakeMaster.getPicture();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];

            InputStream in = blob.getBinaryStream();

            int n = 0;
            while ((n=in.read(buf))>=0)
            {
                baos.write(buf, 0, n);
            }

            in.close();
            byte[] bytes = baos.toByteArray();
            String blobString = new String(bytes);
            cakeMaster.setImage(blobString);
            cakeMasters1List.add(cakeMaster);
        }


        return cakeMasters1List;
    }

/*    @PutMapping(value = "/upload")
    public @ResponseBody CakeMaster handleFileUpload(@RequestParam(value="file") MultipartFile file,@RequestParam(value="id") Long id) throws Exception {
        CakeMaster cakeMaster = cakeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("CakeMaster", "id", id));
        final String UPLOADED_FOLDER = "data/";
        System.out.println(UPLOADED_FOLDER);
        String filePath = UPLOADED_FOLDER + file.getOriginalFilename();
        byte[] bytes = file.getBytes();
        Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
        Files.write(path, bytes);
        copyFileToFolder(file.getOriginalFilename());
        cakeMaster.setPicture(filePath);
        CakeMaster cakeMaster1 = cakeRepository.save(cakeMaster);
        return cakeMaster1;
    }*/


    public void copyFileToFolder(String fileName) throws  Exception{
        String source = env.getProperty("datapath")+"cakemake\\data\\"+fileName;
        String target =env.getProperty("datapath")+"cakemake\\src\\main\\resources\\static\\data\\";
        File sourceFile = new File(source);
        String name = sourceFile.getName();
        File targetFile = new File(target+name);
        System.out.println("Copying file : " + sourceFile.getName() +" from Java Program");
        FileUtils.copyFile(sourceFile, targetFile);
        System.out.println("copying of file from Java program is completed");
    }


    @PostMapping("/register")
    @ResponseBody
    public User register() {
        User user = new User();
        user.setEmail(httpServletRequest.getParameter("email"));
        user.setPassword(httpServletRequest.getParameter("password"));
        user.setConfirmpassword(httpServletRequest.getParameter("confirmpassword"));
        user.setMobile(httpServletRequest.getParameter("mobile"));
        user.setFullname(httpServletRequest.getParameter("fullname"));
        user.setRole("User");

        User user1 = userRepository.save(user);
        return user1;
    }

    @PostMapping("/login")
    @ResponseBody
    public HashMap<String,Object> login() {
        User user1 = userRepository.findUserByEmailAndPassword(httpServletRequest.getParameter("email"),httpServletRequest.getParameter("password"));
        HttpSession session = httpServletRequest.getSession();
        session.removeAttribute("username");
        session.removeAttribute("emailId");
        session.setAttribute("username",user1.getFullname());
        session.setAttribute("emailId",user1.getEmail());
        HttpSession httpSession = httpServletRequest.getSession();


        HashMap<String,Object> hashMap = new HashMap<>();

        if(user1.getRole().equalsIgnoreCase("User")){
            Query query = em.createQuery(
                    "SELECT t FROM Cart t WHERE t.emailId = :emailId");
            query.setParameter("emailId", httpSession.getAttribute("emailId"));
            List<CakeMaster> resultList = query.getResultList();
            hashMap.put("username",httpSession.getAttribute("username"));
            hashMap.put("cartCount",resultList.size());
            hashMap.put("role","User");
        }else{
            Query query = em.createQuery(
                    "SELECT t FROM CakeMaster t");
            List<CakeMaster> resultList = query.getResultList();
            hashMap.put("username",httpSession.getAttribute("username"));
            hashMap.put("cakeMasterCount",resultList.size());
            hashMap.put("role","Admin");
        }

        em.close();

        return hashMap;
    }

    @GetMapping("/home")
    public String home() {
            return "home";
    }



    @GetMapping("/users")
    @ResponseBody
    public Iterable<User> users(){
        return userRepository.findAll();
    }


    @PostMapping("/byPrice")
    @ResponseBody
    public List<CakeMaster> byPrice() {
        Query query = em.createQuery(
                "SELECT e FROM CakeMaster e WHERE e.price BETWEEN :min AND :max ");
        query.setParameter("min", Long.valueOf(httpServletRequest.getParameter("min")));
        query.setParameter("max", Long.valueOf(httpServletRequest.getParameter("max")));
        List<CakeMaster> resultList = query.getResultList();
        em.close();
        return resultList;
    }

    @PostMapping("/byCategory")
    @ResponseBody
    public List<CakeMaster> byCategory() {
        Query query = em.createQuery(
                "SELECT t FROM CakeMaster t WHERE lower(t.category) LIKE lower(CONCAT('%',:searchTerm, '%'))");
        query.setParameter("searchTerm", String.valueOf(httpServletRequest.getParameter("searchTerm")));
        List<CakeMaster> resultList = query.getResultList();
        em.close();
        return resultList;
    }

    @GetMapping("/categoryList")
    @ResponseBody
    public Iterable<Object> categoryList(){
        Query query = em.createQuery(
                "SELECT t.category,count(t.category) FROM CakeMaster t group by t.category");
        List<Object> resultList = query.getResultList();
        List<Object> list = new ArrayList<>();

        for(int i=0; i<resultList.size(); i++) {
            Object[] row = (Object[]) resultList.get(i);
            System.out.println(row[0]+", "+ row[1]);
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("name",row[0]);
            hashMap.put("count",row[1]);
            list.add(hashMap);
        }
        em.close();
        return list;
    }

    @GetMapping("/featureList")
    @ResponseBody
    public Iterable<Object> featureList1(){
        Query query = em.createQuery(
                "SELECT t.feature,count(t.feature) FROM CakeMaster t group by t.feature");
        List<Object> resultList = query.getResultList();
        List<Object> list = new ArrayList<>();

        for(int i=0; i<resultList.size(); i++) {
            Object[] row = (Object[]) resultList.get(i);
            System.out.println(row[0]+", "+ row[1]);
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("name",row[0]);
            hashMap.put("count",row[1]);
            list.add(hashMap);
        }
        em.close();
        return list;
    }
    @PostMapping("/byFeature")
    @ResponseBody
    public List<CakeMaster> byFeature() {
        System.out.println(httpServletRequest.getParameter("feature"));
        Query query = em.createQuery(
                "SELECT t FROM CakeMaster t WHERE t.feature IN (:feature)");
        query.setParameter("feature", String.valueOf(httpServletRequest.getParameter("feature")));
        List<CakeMaster> resultList = query.getResultList();
        em.close();
        return resultList;
    }


    @PostMapping("/addToCart")
    @ResponseBody
    public Cart addCart() {
        HttpSession httpSession = httpServletRequest.getSession();


        CakeMaster cakeMaster2 = cakeRepository.findById(Long.parseLong(httpServletRequest.getParameter("cakeId")))
                .orElseThrow(() -> new ResourceNotFoundException("CakeMaster", "id", Long.parseLong(httpServletRequest.getParameter("cakeId"))));

        Cart cakeMaster = new Cart();
        cakeMaster.setType(httpServletRequest.getParameter("type"));
        cakeMaster.setCategory(httpServletRequest.getParameter("category"));
        cakeMaster.setDescription(httpServletRequest.getParameter("description"));
        cakeMaster.setFeature(httpServletRequest.getParameter("feature"));
        cakeMaster.setFlavor(httpServletRequest.getParameter("flavor"));
        cakeMaster.setName(httpServletRequest.getParameter("name"));
        cakeMaster.setPrice(Long.parseLong(httpServletRequest.getParameter("price")));
        cakeMaster.setQuantity(Long.parseLong(httpServletRequest.getParameter("quantity")));
        cakeMaster.setShape(httpServletRequest.getParameter("shape"));
        cakeMaster.setWeight(httpServletRequest.getParameter("weight"));
        cakeMaster.setEmailId((String) httpSession.getAttribute("emailId"));
        cakeMaster.setCakeId(Long.parseLong(httpServletRequest.getParameter("cakeId")));

        cakeMaster.setDelivery((String) httpSession.getAttribute("delivery"));
        cakeMaster.setDeliveryDate((String) httpSession.getAttribute("deliveryDate"));
        cakeMaster.setMessage((String) httpSession.getAttribute("message"));
        cakeMaster.setMobile((String) httpSession.getAttribute("mobile"));
        cakeMaster.setPicture(cakeMaster2.getPicture());
        cakeMaster.setPaid("No");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = now.format(formatter);
        cakeMaster.setCreated_date(formatDateTime);

        Cart cakeMaster1 = cartRepository.save(cakeMaster);
        return cakeMaster1;
    }

    @PostMapping("/getById")
    @ResponseBody
    public CakeMaster getById(@RequestParam(value="id") Long id) throws Exception{
        CakeMaster cakeMaster = cakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CakeMaster", "id", id));
        Blob blob = cakeMaster.getPicture();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];

        InputStream in = blob.getBinaryStream();

        int n = 0;
        while ((n=in.read(buf))>=0)
        {
            baos.write(buf, 0, n);
        }

        in.close();
        byte[] bytes = baos.toByteArray();
        String blobString = new String(bytes);
        cakeMaster.setImage(blobString);
        return cakeMaster;
    }

    @GetMapping("/cartDetailsApi")
    @ResponseBody
    public Iterable<Cart> cartDetailsApi() {
        HttpSession session = httpServletRequest.getSession();
        System.out.println(String.valueOf(session.getAttribute("emailId")));
        Query query = em.createQuery(
                "SELECT t FROM Cart t WHERE t.emailId = :emailId ");
        query.setParameter("emailId", String.valueOf(session.getAttribute("emailId")));
        List<Cart> resultList = query.getResultList();
        em.close();
        return resultList;
    }


    @GetMapping("/orders")
    @ResponseBody
    public Iterable<Cart> orders() {
        HttpSession session = httpServletRequest.getSession();
        Query query = em.createQuery(
                "SELECT t FROM Cart t WHERE t.paid IN (:paid)");
        query.setParameter("paid", "Yes");
        List<Cart> resultList = query.getResultList();
        em.close();
        return resultList;
    }

    @PostMapping("/logout")
    @ResponseBody
    public HashMap<String,String> logout() {
        HttpSession session = httpServletRequest.getSession();
        session.removeAttribute("username");
        session.removeAttribute("emailId");
        session.invalidate();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("status","200");
        return hashMap;
    }

    @PostMapping("/orderDetailsByEmailId")
    @ResponseBody
    public List<Orders> orderDetailsByEmailId() {
        HttpSession session = httpServletRequest.getSession();
        Query query = em.createQuery(
                "SELECT t FROM Orders t WHERE t.emailId = :emailId");
        query.setParameter("emailId", session.getAttribute("emailId"));
        List<Orders> resultList = query.getResultList();
        return resultList;
    }

    @PostMapping("/orderDetails")
    @ResponseBody
    public List<Orders> orderDetails() {
        HttpSession session = httpServletRequest.getSession();
        Query query = em.createQuery(
                "SELECT t FROM Orders t");
        List<Orders> resultList = query.getResultList();
        return resultList;
    }


    @PostMapping("/addToOrders")
    @ResponseBody
    public Orders addToOrders() {
        HttpSession httpSession = httpServletRequest.getSession();

        CakeMaster cakeMaster2 = cakeRepository.findById(Long.parseLong(httpServletRequest.getParameter("cakeId")))
                .orElseThrow(() -> new ResourceNotFoundException("CakeMaster", "id", Long.parseLong(httpServletRequest.getParameter("cakeId"))));


        Orders order = new Orders();
        order.setType(httpServletRequest.getParameter("type"));
        order.setCategory(httpServletRequest.getParameter("category"));
        order.setDescription(cakeMaster2.getDescription());
        order.setFeature(httpServletRequest.getParameter("feature"));
        order.setFlavor(httpServletRequest.getParameter("flavor"));
        order.setCakeName(cakeMaster2.getName());
        order.setCakePrice(cakeMaster2.getPrice());
        order.setCakeQty(httpServletRequest.getParameter("quantity"));
        order.setShape(httpServletRequest.getParameter("shape"));
        order.setWeight(httpServletRequest.getParameter("weight"));
        order.setEmailId((String) httpSession.getAttribute("emailId"));
        order.setCakeId(Long.parseLong(httpServletRequest.getParameter("cakeId")));
        order.setDelivery(httpServletRequest.getParameter("delivery"));
        order.setDeliveryDate(httpServletRequest.getParameter("deliveryDate"));
        order.setMessage(httpServletRequest.getParameter("message"));
        order.setMobile(httpServletRequest.getParameter("mobile"));
        order.setPicture(cakeMaster2.getPicture());


        order.setPaymentMode(httpServletRequest.getParameter("paymentMode"));
        order.setPaid("Yes");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = now.format(formatter);
        order.setCreated_date(formatDateTime);

        Orders order1 = ordersRepository.save(order);
        return order1;
    }





}
