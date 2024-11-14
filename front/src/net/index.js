import axios  from "axios";
import {ElMessage} from "element-plus";

const authItemName = "access_token"


const  defaultFailure = (message,code,url)=>{
    console.warn(`请求地址 :${url} ，状态码 ${code}，错误信息 ${message} `);
    ElMessage.warning(message)
}
const  defaultError = (err)=>{
    console.error(err)
    ElMessage.warning("发生错误")
}

//保存token
function storeToken(remember,token,expire){
    const authObj ={token:token,expire:expire};
    const str = JSON.stringify(authObj);
    if(remember){
        localStorage.setItem(authItemName,str)
    }else{
        sessionStorage.setItem(authItemName,str);
    }
}

//获取token
function takeToken(){
    const str = localStorage.getItem(authItemName)||sessionStorage.getItem(authItemName);
    if(!str) return null
    const authObj = JSON.parse(str);
    if (authObj.expire <= new Date()){
        deleteToken()
        ElMessage.warning("登录过期，请重新登录")
        return null
    }
    console.log(authObj.token)
    return authObj.token
}

//删除token
function deleteToken(){
    localStorage.removeItem(authItemName);
    sessionStorage.removeItem(authItemName);
}

function post(url,data,success){
    internalPost(url,data,authHeader(),success,failure);
}

function get(url,success,failure){
    internalGet(url,authHeader(),success,failure);
}

function internalPost(url,data,header,success,failure,error = defaultError){
    axios.post(url,data,{headers:header} ).then(({data}) => {
        if (data.code === 200){
            success(data.data);
        }else {
            failure(data.message,data.code,url)
        }
    }).catch(err =>error(err))
}
function internalGet(url,headers,success,failure,error = defaultError){
    axios.get(url,{headers:headers} ).then(({data}) => {
        if (data.code === 200){
            success(data.data);
        }else {
            console.log(data.code)
            failure(data.message,data.code,url)
        }
    }).catch(err =>error(err))
}

function login(username,password,remember,success,failure = defaultFailure){
    internalPost('/api/auth/login',{
        username:username,
        password:password
    },{
        'Content-Type':'application/x-www-form-urlencoded'
        },(data)=>{
        storeToken(remember,data.token,data.expire)
        ElMessage.success(`登录成功，欢迎${data.username}`)
        success(data);
        },failure
    )
}

function logout(success,failure = defaultFailure){
    internalGet('/api/auth/logout',{
        'Authorization': `Bearer ${takeToken()}`
        },() => {
        deleteToken()
        ElMessage.success('退出成功')
        success()
        },failure
    )
}

function authHeader(){
    return {
        'Authorization': `Bearer ${takeToken()}`
    }
}

//是否未登录
function unauthorize(){
    return !takeToken()
}




export {login,logout,post,get,unauthorize}


