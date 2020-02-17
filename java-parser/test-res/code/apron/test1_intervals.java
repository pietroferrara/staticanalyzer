{
	int i, j, k;
	i = 1;
	j = i + 5;
	if(j < 10)
		k = 12;
	else k = -12;
	assert k > 0;
	assert k < 0;//Warn
	assert i > 0;
	assert i == 0;//Warn
	assert j > 0;
	assert j == 5;//Warn
	assert j == 6;
}
