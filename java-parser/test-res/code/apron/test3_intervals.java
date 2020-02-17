{
	int i, j;
	i = 1;
	j = random;
	assert i == 1;
	assert j > 0;//Warn
	assert j <= 0;//Warn
	if(j < 0)
		j = -j;
	while( i < 100) {
		i = i * 2;
		j = j + i;
	}
	assert i > 1;
	assert i >= 100;
	assert j >= 0;
	assert j < 200;//Warn
	assert i < 100;//Warn
	
}
