function loginApi(data) {
  return $axios({
    'url': '/employee/login',
    'method': 'post',
    data //前端带过来的数据
  })
}

function logoutApi(){
  return $axios({
    'url': '/employee/logout',
    'method': 'post',
  })
}
