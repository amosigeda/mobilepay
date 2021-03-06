package com.bracelet.service.impl;

import com.bracelet.datasource.DataSourceChange;
import com.bracelet.entity.CompanyInfo;
import com.bracelet.entity.Step;
import com.bracelet.entity.TokenInfo;
import com.bracelet.service.ITokenInfoService;
import com.bracelet.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

@Service
public class TokenInfoServiceImpl implements ITokenInfoService {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@DataSourceChange(slave = true)
	public Long getUserIdByToken(String token) {
		String sql = "select t_id,token,user_id,createtime  from token_info where token=? order by createtime LIMIT 1";
		List<TokenInfo> list = jdbcTemplate.query(sql, new Object[] { token },
				new BeanPropertyRowMapper<TokenInfo>(TokenInfo.class));
		if (list != null && !list.isEmpty()) {
			return list.get(0).getUser_id();
		} else {
			logger.info("cannot find userId,token:" + token);
		}

		return null;
	}

	@Override
	@DataSourceChange(slave = true)
	public String getTokenByUserId(Long userId) {
		
		String table = "token_info";
		Long jisuan = userId%4;
		if(jisuan == 1){
			table = "token_1_info";
		}else if(jisuan == 2){
			table = "token_2_info";
		}else if(jisuan == 3){
			table = "token_3_info";
		}
		
		String sql = "select t_id,token,user_id,createtime  from  "+ table +"   where user_id=? order by createtime LIMIT 1";
		List<TokenInfo> list = jdbcTemplate.query(sql, new Object[] { userId },
				new BeanPropertyRowMapper<TokenInfo>(TokenInfo.class));
		if (list != null && !list.isEmpty()) {
			return list.get(0).getToken();
		} else {
			logger.info("cannot find token,userId:" + userId);
		}

		return null;
	}


	public String genToken(Long userId) {
		long timestamp = System.currentTimeMillis();
		int randomCode = Utils.randomInt(10, 10000);
		String otoken = "U" + timestamp + userId + "-" + randomCode;
		// md5 签名
		String token = Utils.getMD5(otoken);

		// save to db
		Timestamp now = Utils.getCurrentTimestamp();
		
		String table = "token_info";
		Long jisuan = userId%4;
		if(jisuan == 1){
			table = "token_1_info";
		}else if(jisuan == 2){
			table = "token_2_info";
		}else if(jisuan == 3){
			table = "token_3_info";
		}
		
		jdbcTemplate.update("replace into "+ table + "  (token, user_id, createtime) values (?,?,?)",
				new Object[] { token, userId, now }, new int[] { Types.VARCHAR, Types.INTEGER, Types.TIMESTAMP });
			return token;
	}

	@Override
	public CompanyInfo getScretKeyByCompanyId(Integer id) {
		String sql = "select * from companyinfo where id=?   LIMIT 1";
		List<CompanyInfo> list = jdbcTemplate.query(sql, new Object[] { id }, new BeanPropertyRowMapper<CompanyInfo>(CompanyInfo.class));
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		} else {
			logger.info("getScretKeyByCompanyId return null.user_id:" + id);
		}
		return null;
	}
}
