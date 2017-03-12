<?php
require_once('./conn.php');
require_once('./api.php');

if($_POST['username'] != "" && $_POST['password'] != "") 
{
	$password = md5($_POST['password']);
	
	$sql = "select * from `user` where username='$_POST[username]'";
	$query = mysqli_query($connect, $sql);
	$row = mysqli_num_rows($query);
	if($row == 0) {
		$p = new ServerAPI("pgyu6atqpg7ru", "jbIOOSaggvtmj");
		$r = $p->getToken($_POST['username'], "", "");
		$obj = json_decode($r);
		if($obj->code != 200) {
			$result = array("status" => "error");
			echo json_encode($result);
		}
		else {
			$token = $obj->token;
			$sql2 = "insert into `user` (username, password, token) values('$_POST[username]', '$password', '$token')";
			$query = mysqli_query($connect, $sql2);
			$result = array("status"=>"success", "token"=>$token);
			echo json_encode($result);
		}
	}
	else {
		$result = array("status"=>"exists");
		echo json_encode($result);
	}
}
?>